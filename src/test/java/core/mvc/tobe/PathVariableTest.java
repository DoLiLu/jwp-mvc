package core.mvc.tobe;

import core.mvc.tobe.resolver.ArgumentModel;
import core.mvc.tobe.resolver.ArgumentResolver;
import core.mvc.tobe.resolver.PathVariableArgumentResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

public class PathVariableTest {

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private Method method;
    private static final ParameterNameDiscoverer parameterNameDiscoverer = new LocalVariableTableParameterNameDiscoverer();

    @BeforeEach
    void setup() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        method = pathvariableMethod();
    }

    @ParameterizedTest
    @DisplayName("PathVariable 값을 가져 온다.")
    @ValueSource(strings = {"user, user2"})
    void getPathvariable(String userId) {
        // given
        final ArgumentResolver argumentResolver = PathVariableArgumentResolver.getInstance();
        request.setRequestURI("/users/" + userId);
        ArgumentModel argumentModel = new ArgumentModel(method, method.getParameterTypes()[0], method.getParameterAnnotations()[0],
                Objects.requireNonNull(parameterNameDiscoverer.getParameterNames(method))[0]);

        // when
        final Object argument = argumentResolver.resolve(request, response, argumentModel);

        // then
        assertThat(argument).isEqualTo(userId);
    }

    private Method pathvariableMethod() {
        return Arrays.stream(TestUserController.class.getDeclaredMethods())
                .filter(method -> Objects.equals(method.getName(), "show_pathvariable"))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("찾으시는 Method가 존재하지 않습니다."));
    }
}
