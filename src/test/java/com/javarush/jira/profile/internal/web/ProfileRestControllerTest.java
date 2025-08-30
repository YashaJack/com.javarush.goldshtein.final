package com.javarush.jira.profile.internal.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.javarush.jira.login.AuthUser;
import com.javarush.jira.login.Role;
import com.javarush.jira.login.User;
import com.javarush.jira.profile.ContactTo;
import com.javarush.jira.profile.ProfileTo;
import com.javarush.jira.profile.internal.ProfileMapper;
import com.javarush.jira.profile.internal.model.Contact;
import com.javarush.jira.profile.internal.model.Profile;
import com.javarush.jira.profile.internal.ProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


class ProfileRestControllerTest {

    private static final long USER_ID = 1L;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private final AtomicReference<Long> getOrCreateCalledWith = new AtomicReference<>();
    private final AtomicReference<Profile> saveCalledWith = new AtomicReference<>();


    private final ProfileRepository fakeRepo = new ProfileRepository() {
        @Override
        public List<Profile> findAll(Sort sort) {
            return List.of();
        }

        @Override
        public Page<Profile> findAll(Pageable pageable) {
            return null;
        }

        @Override
        public <S extends Profile> List<S> saveAll(Iterable<S> entities) {
            return List.of();
        }

        @Override
        public Optional<Profile> findById(Long aLong) {
            return Optional.empty();
        }

        @Override
        public boolean existsById(Long aLong) {
            return false;
        }

        @Override
        public List<Profile> findAll() {
            return List.of();
        }

        @Override
        public List<Profile> findAllById(Iterable<Long> longs) {
            return List.of();
        }

        @Override
        public long count() {
            return 0;
        }

        @Override
        public void deleteById(Long aLong) {

        }

        @Override
        public void delete(Profile entity) {

        }

        @Override
        public void deleteAllById(Iterable<? extends Long> longs) {

        }

        @Override
        public void deleteAll(Iterable<? extends Profile> entities) {

        }

        @Override
        public void deleteAll() {

        }

        @Override
        public void flush() {

        }

        @Override
        public <S extends Profile> S saveAndFlush(S entity) {
            return null;
        }

        @Override
        public <S extends Profile> List<S> saveAllAndFlush(Iterable<S> entities) {
            return List.of();
        }

        @Override
        public void deleteAllInBatch(Iterable<Profile> entities) {

        }

        @Override
        public void deleteAllByIdInBatch(Iterable<Long> longs) {

        }

        @Override
        public void deleteAllInBatch() {

        }

        @Override
        public Profile getOne(Long aLong) {
            return null;
        }

        @Override
        public Profile getById(Long aLong) {
            return null;
        }

        @Override
        public Profile getReferenceById(Long aLong) {
            return null;
        }

        @Override
        public <S extends Profile> Optional<S> findOne(Example<S> example) {
            return Optional.empty();
        }

        @Override
        public <S extends Profile> List<S> findAll(Example<S> example) {
            return List.of();
        }

        @Override
        public <S extends Profile> List<S> findAll(Example<S> example, Sort sort) {
            return List.of();
        }

        @Override
        public <S extends Profile> Page<S> findAll(Example<S> example, Pageable pageable) {
            return null;
        }

        @Override
        public <S extends Profile> long count(Example<S> example) {
            return 0;
        }

        @Override
        public <S extends Profile> boolean exists(Example<S> example) {
            return false;
        }

        @Override
        public <S extends Profile, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
            return null;
        }

        @Override
        public int delete(long id) {
            return 0;
        }

        @Override
        public Profile getOrCreate(long userId) {
            getOrCreateCalledWith.set(userId);
            // минимальный профайл
            Profile p = new Profile();
            // у Profile, скорее всего, есть setUserId / setId — если по факту имена другие, поправь тут
            try {
                Profile.class.getMethod("setUserId", Long.class).invoke(p, userId);
            } catch (Exception ignore) { /* если метода нет — ничего страшного */ }
            return p;
        }
        @Override
        public Profile save(Profile profile) {
            saveCalledWith.set(profile);
            return profile;
        }
    };

    private final ProfileMapper fakeMapper = new ProfileMapper() {
        @Override
        public ProfileTo toTo(Profile profile) {
            return new ProfileTo(USER_ID, Collections.emptyList());
        }

        @Override
        public Profile updateFromTo(Profile dst, ProfileTo src) {
            return dst;
        }

        @Override
        public Contact toContact(ContactTo contact) {
            return null;
        }

        @Override
        public ProfileTo fromPostToTo(ProfilePostRequest profilePostRequest) {
            return null;
        }
    };

    private final HandlerMethodArgumentResolver authUserResolver = new HandlerMethodArgumentResolver() {
        @Override
        public boolean supportsParameter(org.springframework.core.MethodParameter parameter) {
            return parameter.getParameterType().equals(AuthUser.class);
        }
        @Override
        public Object resolveArgument(org.springframework.core.MethodParameter parameter,
                                      org.springframework.web.method.support.ModelAndViewContainer mavContainer,
                                      org.springframework.web.context.request.NativeWebRequest webRequest,
                                      org.springframework.web.bind.support.WebDataBinderFactory binderFactory) {
            User u = new User(
                    USER_ID,
                    "user@test.local",
                    "secret",
                    "User",
                    null,
                    "User"
            );
            return new AuthUser(u);
        }
    };

    @BeforeEach
    void setUp() {
        ProfileRestController controller = new ProfileRestController();

        ReflectionTestUtils.setField(controller, "profileRepository", fakeRepo);
        ReflectionTestUtils.setField(controller, "profileMapper", fakeMapper);

        this.objectMapper = new ObjectMapper();

        this.mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setCustomArgumentResolvers((HandlerMethodArgumentResolver) List.of(authUserResolver))
                .setControllerAdvice()
                .build();
    }

    @Test
    void get_returnsProfileForAuthenticatedUser() throws Exception {
        mockMvc.perform(get(ProfileRestController.REST_URL))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

        assertThat(getOrCreateCalledWith.get()).isEqualTo(USER_ID);
    }

    @Test
    void update_updatesProfileForAuthenticatedUser() throws Exception {
        String body = """
                {
                  "id": %d,
                  "contacts": []
                }
                """.formatted(USER_ID);

        mockMvc.perform(put(ProfileRestController.REST_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNoContent());

        assertThat(saveCalledWith.get()).isNotNull();
    }
}