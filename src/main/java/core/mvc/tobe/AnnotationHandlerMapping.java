package core.mvc.tobe;

import com.google.common.collect.Maps;
import core.annotation.web.RequestMapping;
import core.annotation.web.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.*;

import static org.reflections.ReflectionUtils.getAllMethods;
import static org.reflections.util.ReflectionUtilsPredicates.withAnnotation;

public class AnnotationHandlerMapping implements HandlerMapping {
    private final ControllerScanner controllerScanner;

    private final Map<HandlerKey, HandlerExecution> handlerExecutions = Maps.newHashMap();

    public AnnotationHandlerMapping(Object... basePackage) {
        this.controllerScanner = new ControllerScanner(basePackage);
    }

    public void initialize() {
        initHandlerExecutions(controllerScanner.getControllerRegistry());
    }

    private void initHandlerExecutions(ControllerRegistry controllerRegistry) {
        for (Class<?> controllerType : controllerRegistry.getAllTypes()) {
            Object controller = controllerRegistry.getInstanceByType(controllerType);

            Set<Method> requestMappingMethods = getAllMethods(controllerType, withAnnotation(RequestMapping.class));

            saveRequestMappings(controller, requestMappingMethods);
        }
    }

    private void saveRequestMappings(Object controller, Set<Method> requestMappingMethods) {
        for (Method requestMappingMethod : requestMappingMethods) {
            saveRequestMappingAsHandlerExecution(requestMappingMethod, controller);
        }
    }

    private void saveRequestMappingAsHandlerExecution(Method requestMappingMethod, Object handler) {
        RequestMapping requestMapping = requestMappingMethod.getDeclaredAnnotation(RequestMapping.class);

        for (RequestMethod requestMethod : getRequestMethods(requestMapping)) {
            HandlerKey handlerKey = new HandlerKey(requestMapping.value(), requestMethod);

            this.handlerExecutions.put(handlerKey, new HandlerExecution(handler, requestMappingMethod));
        }
    }

    private List<RequestMethod> getRequestMethods(RequestMapping requestMapping) {
        List<RequestMethod> methods = Arrays.asList(requestMapping.method());

        if (methods.isEmpty()) {
            return Arrays.asList(RequestMethod.values());
        }

        return methods;
    }

    public HandlerExecution getHandler(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        RequestMethod rm = RequestMethod.valueOf(request.getMethod().toUpperCase());
        return handlerExecutions.get(new HandlerKey(requestUri, rm));
    }
}
