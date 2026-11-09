package net.genie.gateway.service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import net.genie.gateway.config.Constants;
import net.genie.gateway.domain.Authority;
import net.genie.gateway.domain.User;
import net.genie.gateway.repository.AuthorityRepository;
import net.genie.gateway.repository.UserRepository;
import net.genie.gateway.security.SecurityUtils;
import net.genie.gateway.service.dto.AdminUserDTO;
import net.genie.gateway.service.dto.UserDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service class for managing users.
 */
@Service
public class UserService {

    private static final Logger LOG = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;

    private final AuthorityRepository authorityRepository;
    
    private static final String UPDATED_AT = "updated_at";


    public UserService(UserRepository userRepository, AuthorityRepository authorityRepository) {
        this.userRepository = userRepository;
        this.authorityRepository = authorityRepository;
    }

    /**
     * Update basic information (first name, last name, email, language) for the current user.
     *
     * @param firstName first name of user.
     * @param lastName  last name of user.
     * @param email     email of user.
     * @param langKey   language key.
     * @param imageUrl  image URL of user.
     * @return a completed {@link Mono}.
     */
    @Transactional
    public Mono<Void> updateUser(String firstName, String lastName, String email, String langKey, String imageUrl) {
        return SecurityUtils.getCurrentUserLogin()
            .flatMap(userRepository::findOneByLogin)
            .flatMap(user -> {
                user.setFirstName(firstName);
                user.setLastName(lastName);
                if (email != null) {
                    user.setEmail(email.toLowerCase());
                }
                user.setLangKey(langKey);
                user.setImageUrl(imageUrl);
                return saveUser(user);
            })
            .doOnNext(user -> LOG.debug("Changed Information for User: {}", user))
            .then();
    }

    @Transactional
    public Mono<User> saveUser(User user) {
        return saveUser(user, false);
    }

    @Transactional
    public Mono<User> saveUser(User user, boolean forceCreate) {
        return SecurityUtils.getCurrentUserLogin()
            .switchIfEmpty(Mono.just(Constants.SYSTEM))
            .flatMap(login -> {
                if (user.getCreatedBy() == null) {
                    user.setCreatedBy(login);
                }
                user.setLastModifiedBy(login);
                // Saving the relationship can be done in an entity callback
                // once https://github.com/spring-projects/spring-data-r2dbc/issues/215 is done
                Mono<User> persistedUser;
                if (forceCreate) {
                    persistedUser = userRepository.create(user);
                } else {
                    persistedUser = userRepository.save(user);
                }
                return persistedUser.flatMap(savedUser ->
                    Flux.fromIterable(user.getAuthorities())
                        .flatMap(authority -> userRepository.saveUserAuthority(savedUser.getId(), authority.getName()))
                        .then(Mono.just(savedUser))
                );
            });
    }

    @Transactional(readOnly = true)
    public Flux<AdminUserDTO> getAllManagedUsers(Pageable pageable) {
        return userRepository.findAllWithAuthorities(pageable).map(AdminUserDTO::new);
    }

    @Transactional(readOnly = true)
    public Flux<UserDTO> getAllPublicUsers(Pageable pageable) {
        return userRepository.findAllByIdNotNullAndActivatedIsTrue(pageable).map(UserDTO::new);
    }

    @Transactional(readOnly = true)
    public Mono<Long> countManagedUsers() {
        return userRepository.count();
    }

    @Transactional(readOnly = true)
    public Mono<User> getUserWithAuthoritiesByLogin(String login) {
        return userRepository.findOneWithAuthoritiesByLogin(login);
    }

    /**
     * Gets a list of all the authorities.
     * @return a list of all the authorities.
     */
    @Transactional(readOnly = true)
    public Flux<String> getAuthorities() {
        return authorityRepository.findAll().map(Authority::getName);
    }

    private Mono<User> syncUserWithIdP(Map<String, Object> details, User user) {
        // save authorities in to sync user roles/groups between IdP and JHipster's local database
        Collection<String> userAuthorities = user.getAuthorities().stream().map(Authority::getName).toList();

        return getAuthorities()
            .collectList()
            .flatMapMany(dbAuthorities -> {
                List<Authority> authoritiesToSave = userAuthorities
                    .stream()
                    .filter(authority -> !dbAuthorities.contains(authority))
                    .map(authority -> {
                        Authority authorityToSave = new Authority();
                        authorityToSave.setName(authority);
                        return authorityToSave;
                    })
                    .toList();
                return Flux.fromIterable(authoritiesToSave);
            })
            .doOnNext(authority -> LOG.debug("Saving authority '{}' in local database", authority))
            .flatMap(authorityRepository::save)
            .then(userRepository.findOneByLogin(user.getLogin()))
            .switchIfEmpty(saveUser(user, true))
            .flatMap(existingUser -> {
                // if IdP sends last updated information, use it to determine if an update should happen
                if (details.get(UPDATED_AT) != null) {
                    Instant dbModifiedDate = existingUser.getLastModifiedDate();
                    Instant idpModifiedDate;
                    if (details.get(UPDATED_AT) instanceof Instant instant) {
                        idpModifiedDate = instant;
                    } else {
                        idpModifiedDate = Instant.ofEpochSecond((Integer) details.get(UPDATED_AT));
                    }
                    if (idpModifiedDate.isAfter(dbModifiedDate)) {
                        LOG.debug("Updating user '{}' in local database", user.getLogin());
                        return updateUser(user.getFirstName(), user.getLastName(), user.getEmail(), user.getLangKey(), user.getImageUrl());
                    }
                } else {
                    LOG.debug("Updating user '{}' in local database", user.getLogin());
                    return updateUser(user.getFirstName(), user.getLastName(), user.getEmail(), user.getLangKey(), user.getImageUrl());
                }
                return Mono.empty();
            })
            .thenReturn(user);
    }

    /**
     * Returns the user from an OAuth 2.0 login or resource server with JWT.
     * Synchronizes the user in the local repository.
     *
     * @param authToken the authentication token.
     * @return the user from the authentication.
     */
    @Transactional
    public Mono<AdminUserDTO> getUserFromAuthentication(AbstractAuthenticationToken authToken) {
        Map<String, Object> attributes;
        switch (authToken) {
          case OAuth2AuthenticationToken oAuth2AuthToken -> 
              attributes = oAuth2AuthToken.getPrincipal().getAttributes();
          
          case JwtAuthenticationToken jwtAuthToken -> 
              attributes = jwtAuthToken.getTokenAttributes();
          
          default -> 
              throw new IllegalArgumentException("AuthenticationToken is not OAuth2 nor JWT!");
        }
        
        
        User user = getUser(attributes);
        user.setAuthorities(
            authToken
                .getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .map(authority -> {
                    Authority auth = new Authority();
                    auth.setName(authority);
                    return auth;
                })
                .collect(Collectors.toSet())
        );

        return syncUserWithIdP(attributes, user).map(AdminUserDTO::new);
    }


private static User getUser(Map<String, Object> details) {
    User user = new User();

    String sub = String.valueOf(details.get("sub"));
    String username = getUsername(details);

    setIdentity(user, details, sub, username);
    setPersonalDetails(user, details);
    user.setEmail(getEmail(details, sub, username));
    user.setLangKey(getLanguage(details));
    user.setImageUrl((String) details.get("picture"));
    user.setActivated(getActivated(details));

    return user;
}

private static String getUsername(Map<String, Object> details) {
    return Optional.ofNullable((String) details.get("preferred_username"))
        .map(String::toLowerCase)
        .orElse(null);
}

private static void setIdentity(
    User user,
    Map<String, Object> details,
    String sub,
    String username
) {
    String uid = (String) details.get("uid");

    // Resource server JWT: sub is email and uid is ID.
    user.setId(uid != null ? uid : sub);
    
    String login;

    if (username != null) {
        login = username;
    } else if (uid != null) {
        login = sub;
    } else {
        login = user.getId();
    }

    user.setLogin(login);
}

private static void setPersonalDetails(User user, Map<String, Object> details) {
    String firstName = (String) details.get("given_name");

    if (firstName == null) {
        firstName = (String) details.get("name");
    }

    user.setFirstName(firstName);
    user.setLastName((String) details.get("family_name"));
}

private static String getEmail(
    Map<String, Object> details,
    String sub,
    String username
) {
    String email = (String) details.get("email");

    if (email != null) {
        return email.toLowerCase();
    }

    // Special handling for Auth0.
    if (sub.contains("|") && username != null && username.contains("@")) {
        return username;
    }

    return sub;
}

private static String getLanguage(Map<String, Object> details) {
    String langKey = (String) details.get("langKey");

    if (langKey != null) {
        return langKey;
    }

    String locale = (String) details.get("locale");

    if (locale == null) {
        return Constants.DEFAULT_LANGUAGE;
    }

    // Trim off country code if it exists.
    return locale.split("[_-]", 2)[0].toLowerCase();
}

private static Boolean getActivated(Map<String, Object> details) {
    return (Boolean) details.getOrDefault("email_verified", Boolean.TRUE);
}

}
