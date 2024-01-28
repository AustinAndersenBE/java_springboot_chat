

Spring Boot is well suited for web application development. You can create a self-contained HTTP server by using embedded Tomcat, Jetty, Undertow, or Netty. Most web applications use the spring-boot-starter-web module to get up and running quickly. You can also choose to build reactive web applications by using the spring-boot-starter-webflux module.

If you have not yet developed a Spring Boot web application, you can follow the "Hello World!" example in the Getting started section.
1. Servlet Web Applications

If you want to build servlet-based web applications, you can take advantage of Spring Boot’s auto-configuration for Spring MVC or Jersey.
1.1. The “Spring Web MVC Framework”

The Spring Web MVC framework (often referred to as “Spring MVC”) is a rich “model view controller” web framework. Spring MVC lets you create special @Controller or @RestController beans to handle incoming HTTP requests. Methods in your controller are mapped to HTTP by using @RequestMapping annotations.

The following code shows a typical @RestController that serves JSON data:
Java
Kotlin

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class MyRestController {

    private final UserRepository userRepository;

    private final CustomerRepository customerRepository;

    public MyRestController(UserRepository userRepository, CustomerRepository customerRepository) {
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
    }

    @GetMapping("/{userId}")
    public User getUser(@PathVariable Long userId) {
        return this.userRepository.findById(userId).get();
    }

    @GetMapping("/{userId}/customers")
    public List<Customer> getUserCustomers(@PathVariable Long userId) {
        return this.userRepository.findById(userId).map(this.customerRepository::findByUser).get();
    }

    @DeleteMapping("/{userId}")
    public void deleteUser(@PathVariable Long userId) {
        this.userRepository.deleteById(userId);
    }

}

“WebMvc.fn”, the functional variant, separates the routing configuration from the actual handling of the requests, as shown in the following example:
Java
Kotlin

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.function.RequestPredicate;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import static org.springframework.web.servlet.function.RequestPredicates.accept;
import static org.springframework.web.servlet.function.RouterFunctions.route;

@Configuration(proxyBeanMethods = false)
public class MyRoutingConfiguration {

    private static final RequestPredicate ACCEPT_JSON = accept(MediaType.APPLICATION_JSON);

    @Bean
    public RouterFunction<ServerResponse> routerFunction(MyUserHandler userHandler) {
        return route()
                .GET("/{user}", ACCEPT_JSON, userHandler::getUser)
                .GET("/{user}/customers", ACCEPT_JSON, userHandler::getUserCustomers)
                .DELETE("/{user}", ACCEPT_JSON, userHandler::deleteUser)
                .build();
    }

}

Java
Kotlin

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

@Component
public class MyUserHandler {

    public ServerResponse getUser(ServerRequest request) {
        ...
        return ServerResponse.ok().build();
    }

    public ServerResponse getUserCustomers(ServerRequest request) {
        ...
        return ServerResponse.ok().build();
    }

    public ServerResponse deleteUser(ServerRequest request) {
        ...
        return ServerResponse.ok().build();
    }

}

Spring MVC is part of the core Spring Framework, and detailed information is available in the reference documentation. There are also several guides that cover Spring MVC available at spring.io/guides.
	You can define as many RouterFunction beans as you like to modularize the definition of the router. Beans can be ordered if you need to apply a precedence.
1.1.1. Spring MVC Auto-configuration

Spring Boot provides auto-configuration for Spring MVC that works well with most applications. It replaces the need for @EnableWebMvc and the two cannot be used together. In addition to Spring MVC’s defaults, the auto-configuration provides the following features:

    Inclusion of ContentNegotiatingViewResolver and BeanNameViewResolver beans.

    Support for serving static resources, including support for WebJars (covered later in this document).

    Automatic registration of Converter, GenericConverter, and Formatter beans.

    Support for HttpMessageConverters (covered later in this document).

    Automatic registration of MessageCodesResolver (covered later in this document).

    Static index.html support.

    Automatic use of a ConfigurableWebBindingInitializer bean (covered later in this document).

If you want to keep those Spring Boot MVC customizations and make more MVC customizations (interceptors, formatters, view controllers, and other features), you can add your own @Configuration class of type WebMvcConfigurer but without @EnableWebMvc.

If you want to provide custom instances of RequestMappingHandlerMapping, RequestMappingHandlerAdapter, or ExceptionHandlerExceptionResolver, and still keep the Spring Boot MVC customizations, you can declare a bean of type WebMvcRegistrations and use it to provide custom instances of those components. The custom instances will be subject to further initialization and configuration by Spring MVC. To participate in, and if desired, override that subsequent processing, a WebMvcConfigurer should be used.

If you do not want to use the auto-configuration and want to take complete control of Spring MVC, add your own @Configuration annotated with @EnableWebMvc. Alternatively, add your own @Configuration-annotated DelegatingWebMvcConfiguration as described in the Javadoc of @EnableWebMvc.
1.1.2. Spring MVC Conversion Service

Spring MVC uses a different ConversionService to the one used to convert values from your application.properties or application.yaml file. It means that Period, Duration and DataSize converters are not available and that @DurationUnit and @DataSizeUnit annotations will be ignored.

If you want to customize the ConversionService used by Spring MVC, you can provide a WebMvcConfigurer bean with an addFormatters method. From this method you can register any converter that you like, or you can delegate to the static methods available on ApplicationConversionService.

Conversion can also be customized using the spring.mvc.format.* configuration properties. When not configured, the following defaults are used:
Property 	DateTimeFormatter

spring.mvc.format.date
	

ofLocalizedDate(FormatStyle.SHORT)

spring.mvc.format.time
	

ofLocalizedTime(FormatStyle.SHORT)

spring.mvc.format.date-time
	

ofLocalizedDateTime(FormatStyle.SHORT)
1.1.3. HttpMessageConverters

Spring MVC uses the HttpMessageConverter interface to convert HTTP requests and responses. Sensible defaults are included out of the box. For example, objects can be automatically converted to JSON (by using the Jackson library) or XML (by using the Jackson XML extension, if available, or by using JAXB if the Jackson XML extension is not available). By default, strings are encoded in UTF-8.

If you need to add or customize converters, you can use Spring Boot’s HttpMessageConverters class, as shown in the following listing:
Java
Kotlin

import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;

@Configuration(proxyBeanMethods = false)
public class MyHttpMessageConvertersConfiguration {

    @Bean
    public HttpMessageConverters customConverters() {
        HttpMessageConverter<?> additional = new AdditionalHttpMessageConverter();
        HttpMessageConverter<?> another = new AnotherHttpMessageConverter();
        return new HttpMessageConverters(additional, another);
    }

}

Any HttpMessageConverter bean that is present in the context is added to the list of converters. You can also override default converters in the same way.
1.1.4. MessageCodesResolver

Spring MVC has a strategy for generating error codes for rendering error messages from binding errors: MessageCodesResolver. If you set the spring.mvc.message-codes-resolver-format property PREFIX_ERROR_CODE or POSTFIX_ERROR_CODE, Spring Boot creates one for you (see the enumeration in DefaultMessageCodesResolver.Format).
1.1.5. Static Content

By default, Spring Boot serves static content from a directory called /static (or /public or /resources or /META-INF/resources) in the classpath or from the root of the ServletContext. It uses the ResourceHttpRequestHandler from Spring MVC so that you can modify that behavior by adding your own WebMvcConfigurer and overriding the addResourceHandlers method.

In a stand-alone web application, the default servlet from the container is not enabled. It can be enabled using the server.servlet.register-default-servlet property.

The default servlet acts as a fallback, serving content from the root of the ServletContext if Spring decides not to handle it. Most of the time, this does not happen (unless you modify the default MVC configuration), because Spring can always handle requests through the DispatcherServlet.

By default, resources are mapped on /**, but you can tune that with the spring.mvc.static-path-pattern property. For instance, relocating all resources to /resources/** can be achieved as follows:
Properties
Yaml

spring.mvc.static-path-pattern=/resources/**

You can also customize the static resource locations by using the spring.web.resources.static-locations property (replacing the default values with a list of directory locations). The root servlet context path, "/", is automatically added as a location as well.

In addition to the “standard” static resource locations mentioned earlier, a special case is made for Webjars content. By default, any resources with a path in /webjars/** are served from jar files if they are packaged in the Webjars format. The path can be customized with the spring.mvc.webjars-path-pattern property.
	Do not use the src/main/webapp directory if your application is packaged as a jar. Although this directory is a common standard, it works only with war packaging, and it is silently ignored by most build tools if you generate a jar.

Spring Boot also supports the advanced resource handling features provided by Spring MVC, allowing use cases such as cache-busting static resources or using version agnostic URLs for Webjars.

To use version agnostic URLs for Webjars, add the webjars-locator-core dependency. Then declare your Webjar. Using jQuery as an example, adding "/webjars/jquery/jquery.min.js" results in "/webjars/jquery/x.y.z/jquery.min.js" where x.y.z is the Webjar version.
	If you use JBoss, you need to declare the webjars-locator-jboss-vfs dependency instead of the webjars-locator-core. Otherwise, all Webjars resolve as a 404.

To use cache busting, the following configuration configures a cache busting solution for all static resources, effectively adding a content hash, such as <link href="/css/spring-2a2d595e6ed9a0b24f027f2b63b134d6.css"/>, in URLs:
Properties
Yaml

spring.web.resources.chain.strategy.content.enabled=true
spring.web.resources.chain.strategy.content.paths=/**

	Links to resources are rewritten in templates at runtime, thanks to a ResourceUrlEncodingFilter that is auto-configured for Thymeleaf and FreeMarker. You should manually declare this filter when using JSPs. Other template engines are currently not automatically supported but can be with custom template macros/helpers and the use of the ResourceUrlProvider.

When loading resources dynamically with, for example, a JavaScript module loader, renaming files is not an option. That is why other strategies are also supported and can be combined. A "fixed" strategy adds a static version string in the URL without changing the file name, as shown in the following example:
Properties
Yaml

spring.web.resources.chain.strategy.content.enabled=true
spring.web.resources.chain.strategy.content.paths=/**
spring.web.resources.chain.strategy.fixed.enabled=true
spring.web.resources.chain.strategy.fixed.paths=/js/lib/
spring.web.resources.chain.strategy.fixed.version=v12

With this configuration, JavaScript modules located under "/js/lib/" use a fixed versioning strategy ("/v12/js/lib/mymodule.js"), while other resources still use the content one (<link href="/css/spring-2a2d595e6ed9a0b24f027f2b63b134d6.css"/>).

See WebProperties.Resources for more supported options.
	

This feature has been thoroughly described in a dedicated blog post and in Spring Framework’s reference documentation.
1.1.6. Welcome Page

Spring Boot supports both static and templated welcome pages. It first looks for an index.html file in the configured static content locations. If one is not found, it then looks for an index template. If either is found, it is automatically used as the welcome page of the application.

This only acts as a fallback for actual index routes defined by the application. The ordering is defined by the order of HandlerMapping beans which is by default the following:

RouterFunctionMapping
	

Endpoints declared with RouterFunction beans

RequestMappingHandlerMapping
	

Endpoints declared in @Controller beans

WelcomePageHandlerMapping
	

The welcome page support
1.1.7. Custom Favicon

As with other static resources, Spring Boot checks for a favicon.ico in the configured static content locations. If such a file is present, it is automatically used as the favicon of the application.
1.1.8. Path Matching and Content Negotiation

Spring MVC can map incoming HTTP requests to handlers by looking at the request path and matching it to the mappings defined in your application (for example, @GetMapping annotations on Controller methods).

Spring Boot chooses to disable suffix pattern matching by default, which means that requests like "GET /projects/spring-boot.json" will not be matched to @GetMapping("/projects/spring-boot") mappings. This is considered as a best practice for Spring MVC applications. This feature was mainly useful in the past for HTTP clients which did not send proper "Accept" request headers; we needed to make sure to send the correct Content Type to the client. Nowadays, Content Negotiation is much more reliable.

There are other ways to deal with HTTP clients that do not consistently send proper "Accept" request headers. Instead of using suffix matching, we can use a query parameter to ensure that requests like "GET /projects/spring-boot?format=json" will be mapped to @GetMapping("/projects/spring-boot"):
Properties
Yaml

spring.mvc.contentnegotiation.favor-parameter=true

Or if you prefer to use a different parameter name:
Properties
Yaml

spring.mvc.contentnegotiation.favor-parameter=true
spring.mvc.contentnegotiation.parameter-name=myparam

Most standard media types are supported out-of-the-box, but you can also define new ones:
Properties
Yaml

spring.mvc.contentnegotiation.media-types.markdown=text/markdown

As of Spring Framework 5.3, Spring MVC supports two strategies for matching request paths to controllers. By default, Spring Boot uses the PathPatternParser strategy. PathPatternParser is an optimized implementation but comes with some restrictions compared to the AntPathMatcher strategy. PathPatternParser restricts usage of some path pattern variants. It is also incompatible with configuring the DispatcherServlet with a path prefix (spring.mvc.servlet.path).

The strategy can be configured using the spring.mvc.pathmatch.matching-strategy configuration property, as shown in the following example:
Properties
Yaml

spring.mvc.pathmatch.matching-strategy=ant-path-matcher

By default, Spring MVC will send a 404 Not Found error response if a handler is not found for a request. To have a NoHandlerFoundException thrown instead, set configprop:spring.mvc.throw-exception-if-no-handler-found to true. Note that, by default, the serving of static content is mapped to /** and will, therefore, provide a handler for all requests. For a NoHandlerFoundException to be thrown, you must also set spring.mvc.static-path-pattern to a more specific value such as /resources/** or set spring.web.resources.add-mappings to false to disable serving of static content entirely.
1.1.9. ConfigurableWebBindingInitializer

Spring MVC uses a WebBindingInitializer to initialize a WebDataBinder for a particular request. If you create your own ConfigurableWebBindingInitializer @Bean, Spring Boot automatically configures Spring MVC to use it.
1.1.10. Template Engines

As well as REST web services, you can also use Spring MVC to serve dynamic HTML content. Spring MVC supports a variety of templating technologies, including Thymeleaf, FreeMarker, and JSPs. Also, many other templating engines include their own Spring MVC integrations.

Spring Boot includes auto-configuration support for the following templating engines:

    FreeMarker

    Groovy

    Thymeleaf

    Mustache

	If possible, JSPs should be avoided. There are several known limitations when using them with embedded servlet containers.

When you use one of these templating engines with the default configuration, your templates are picked up automatically from src/main/resources/templates.
	Depending on how you run your application, your IDE may order the classpath differently. Running your application in the IDE from its main method results in a different ordering than when you run your application by using Maven or Gradle or from its packaged jar. This can cause Spring Boot to fail to find the expected template. If you have this problem, you can reorder the classpath in the IDE to place the module’s classes and resources first.
1.1.11. Error Handling

By default, Spring Boot provides an /error mapping that handles all errors in a sensible way, and it is registered as a “global” error page in the servlet container. For machine clients, it produces a JSON response with details of the error, the HTTP status, and the exception message. For browser clients, there is a “whitelabel” error view that renders the same data in HTML format (to customize it, add a View that resolves to error).

There are a number of server.error properties that can be set if you want to customize the default error handling behavior. See the “Server Properties” section of the Appendix.

To replace the default behavior completely, you can implement ErrorController and register a bean definition of that type or add a bean of type ErrorAttributes to use the existing mechanism but replace the contents.
	The BasicErrorController can be used as a base class for a custom ErrorController. This is particularly useful if you want to add a handler for a new content type (the default is to handle text/html specifically and provide a fallback for everything else). To do so, extend BasicErrorController, add a public method with a @RequestMapping that has a produces attribute, and create a bean of your new type.

As of Spring Framework 6.0, RFC 7807 Problem Details is supported. Spring MVC can produce custom error messages with the application/problem+json media type, like:

{
  "type": "https://example.org/problems/unknown-project",
  "title": "Unknown project",
  "status": 404,
  "detail": "No project found for id 'spring-unknown'",
  "instance": "/projects/spring-unknown"
}

This support can be enabled by setting spring.mvc.problemdetails.enabled to true.

You can also define a class annotated with @ControllerAdvice to customize the JSON document to return for a particular controller and/or exception type, as shown in the following example:
Java
Kotlin

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice(basePackageClasses = SomeController.class)
public class MyControllerAdvice extends ResponseEntityExceptionHandler {

    @ResponseBody
    @ExceptionHandler(MyException.class)
    public ResponseEntity<?> handleControllerException(HttpServletRequest request, Throwable ex) {
        HttpStatus status = getStatus(request);
        return new ResponseEntity<>(new MyErrorBody(status.value(), ex.getMessage()), status);
    }

    private HttpStatus getStatus(HttpServletRequest request) {
        Integer code = (Integer) request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        HttpStatus status = HttpStatus.resolve(code);
        return (status != null) ? status : HttpStatus.INTERNAL_SERVER_ERROR;
    }

}

In the preceding example, if MyException is thrown by a controller defined in the same package as SomeController, a JSON representation of the MyErrorBody POJO is used instead of the ErrorAttributes representation.

In some cases, errors handled at the controller level are not recorded by web observations or the metrics infrastructure. Applications can ensure that such exceptions are recorded with the observations by setting the handled exception on the observation context.
Custom Error Pages

If you want to display a custom HTML error page for a given status code, you can add a file to an /error directory. Error pages can either be static HTML (that is, added under any of the static resource directories) or be built by using templates. The name of the file should be the exact status code or a series mask.

For example, to map 404 to a static HTML file, your directory structure would be as follows:

src/
 +- main/
     +- java/
     |   + <source code>
     +- resources/
         +- public/
             +- error/
             |   +- 404.html
             +- <other public assets>

To map all 5xx errors by using a FreeMarker template, your directory structure would be as follows:

src/
 +- main/
     +- java/
     |   + <source code>
     +- resources/
         +- templates/
             +- error/
             |   +- 5xx.ftlh
             +- <other templates>

For more complex mappings, you can also add beans that implement the ErrorViewResolver interface, as shown in the following example:
Java
Kotlin

import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.boot.autoconfigure.web.servlet.error.ErrorViewResolver;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.ModelAndView;

public class MyErrorViewResolver implements ErrorViewResolver {

    @Override
    public ModelAndView resolveErrorView(HttpServletRequest request, HttpStatus status, Map<String, Object> model) {
        // Use the request or status to optionally return a ModelAndView
        if (status == HttpStatus.INSUFFICIENT_STORAGE) {
            // We could add custom model values here
            new ModelAndView("myview");
        }
        return null;
    }

}

You can also use regular Spring MVC features such as @ExceptionHandler methods and @ControllerAdvice. The ErrorController then picks up any unhandled exceptions.
Mapping Error Pages Outside of Spring MVC

For applications that do not use Spring MVC, you can use the ErrorPageRegistrar interface to directly register ErrorPages. This abstraction works directly with the underlying embedded servlet container and works even if you do not have a Spring MVC DispatcherServlet.
Java
Kotlin

import org.springframework.boot.web.server.ErrorPage;
import org.springframework.boot.web.server.ErrorPageRegistrar;
import org.springframework.boot.web.server.ErrorPageRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;

@Configuration(proxyBeanMethods = false)
public class MyErrorPagesConfiguration {

    @Bean
    public ErrorPageRegistrar errorPageRegistrar() {
        return this::registerErrorPages;
    }

    private void registerErrorPages(ErrorPageRegistry registry) {
        registry.addErrorPages(new ErrorPage(HttpStatus.BAD_REQUEST, "/400"));
    }

}

	If you register an ErrorPage with a path that ends up being handled by a Filter (as is common with some non-Spring web frameworks, like Jersey and Wicket), then the Filter has to be explicitly registered as an ERROR dispatcher, as shown in the following example:
Java
Kotlin

import java.util.EnumSet;

import jakarta.servlet.DispatcherType;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class MyFilterConfiguration {

    @Bean
    public FilterRegistrationBean<MyFilter> myFilter() {
        FilterRegistrationBean<MyFilter> registration = new FilterRegistrationBean<>(new MyFilter());
        // ...
        registration.setDispatcherTypes(EnumSet.allOf(DispatcherType.class));
        return registration;
    }

}

Note that the default FilterRegistrationBean does not include the ERROR dispatcher type.
Error Handling in a WAR Deployment

When deployed to a servlet container, Spring Boot uses its error page filter to forward a request with an error status to the appropriate error page. This is necessary as the servlet specification does not provide an API for registering error pages. Depending on the container that you are deploying your war file to and the technologies that your application uses, some additional configuration may be required.

The error page filter can only forward the request to the correct error page if the response has not already been committed. By default, WebSphere Application Server 8.0 and later commits the response upon successful completion of a servlet’s service method. You should disable this behavior by setting com.ibm.ws.webcontainer.invokeFlushAfterService to false.
1.1.12. CORS Support

Cross-origin resource sharing (CORS) is a W3C specification implemented by most browsers that lets you specify in a flexible way what kind of cross-domain requests are authorized, instead of using some less secure and less powerful approaches such as IFRAME or JSONP.

As of version 4.2, Spring MVC supports CORS. Using controller method CORS configuration with @CrossOrigin annotations in your Spring Boot application does not require any specific configuration. Global CORS configuration can be defined by registering a WebMvcConfigurer bean with a customized addCorsMappings(CorsRegistry) method, as shown in the following example:
Java
Kotlin

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration(proxyBeanMethods = false)
public class MyCorsConfiguration {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {

            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**");
            }

        };
    }

}

1.2. JAX-RS and Jersey

If you prefer the JAX-RS programming model for REST endpoints, you can use one of the available implementations instead of Spring MVC. Jersey and Apache CXF work quite well out of the box. CXF requires you to register its Servlet or Filter as a @Bean in your application context. Jersey has some native Spring support, so we also provide auto-configuration support for it in Spring Boot, together with a starter.

To get started with Jersey, include the spring-boot-starter-jersey as a dependency and then you need one @Bean of type ResourceConfig in which you register all the endpoints, as shown in the following example:

import org.glassfish.jersey.server.ResourceConfig;

import org.springframework.stereotype.Component;

@Component
public class MyJerseyConfig extends ResourceConfig {

    public MyJerseyConfig() {
        register(MyEndpoint.class);
    }

}

	Jersey’s support for scanning executable archives is rather limited. For example, it cannot scan for endpoints in a package found in a fully executable jar file or in WEB-INF/classes when running an executable war file. To avoid this limitation, the packages method should not be used, and endpoints should be registered individually by using the register method, as shown in the preceding example.

For more advanced customizations, you can also register an arbitrary number of beans that implement ResourceConfigCustomizer.

All the registered endpoints should be @Components with HTTP resource annotations (@GET and others), as shown in the following example:

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import org.springframework.stereotype.Component;

@Component
@Path("/hello")
public class MyEndpoint {

    @GET
    public String message() {
        return "Hello";
    }

}

Since the Endpoint is a Spring @Component, its lifecycle is managed by Spring and you can use the @Autowired annotation to inject dependencies and use the @Value annotation to inject external configuration. By default, the Jersey servlet is registered and mapped to /*. You can change the mapping by adding @ApplicationPath to your ResourceConfig.

By default, Jersey is set up as a servlet in a @Bean of type ServletRegistrationBean named jerseyServletRegistration. By default, the servlet is initialized lazily, but you can customize that behavior by setting spring.jersey.servlet.load-on-startup. You can disable or override that bean by creating one of your own with the same name. You can also use a filter instead of a servlet by setting spring.jersey.type=filter (in which case, the @Bean to replace or override is jerseyFilterRegistration). The filter has an @Order, which you can set with spring.jersey.filter.order. When using Jersey as a filter, a servlet that will handle any requests that are not intercepted by Jersey must be present. If your application does not contain such a servlet, you may want to enable the default servlet by setting server.servlet.register-default-servlet to true. Both the servlet and the filter registrations can be given init parameters by using spring.jersey.init.* to specify a map of properties.
1.3. Embedded Servlet Container Support

For servlet application, Spring Boot includes support for embedded Tomcat, Jetty, and Undertow servers. Most developers use the appropriate “Starter” to obtain a fully configured instance. By default, the embedded server listens for HTTP requests on port 8080.
1.3.1. Servlets, Filters, and Listeners

When using an embedded servlet container, you can register servlets, filters, and all the listeners (such as HttpSessionListener) from the servlet spec, either by using Spring beans or by scanning for servlet components.
Registering Servlets, Filters, and Listeners as Spring Beans

Any Servlet, Filter, or servlet *Listener instance that is a Spring bean is registered with the embedded container. This can be particularly convenient if you want to refer to a value from your application.properties during configuration.

By default, if the context contains only a single Servlet, it is mapped to /. In the case of multiple servlet beans, the bean name is used as a path prefix. Filters map to /*.

If convention-based mapping is not flexible enough, you can use the ServletRegistrationBean, FilterRegistrationBean, and ServletListenerRegistrationBean classes for complete control.

It is usually safe to leave filter beans unordered. If a specific order is required, you should annotate the Filter with @Order or make it implement Ordered. You cannot configure the order of a Filter by annotating its bean method with @Order. If you cannot change the Filter class to add @Order or implement Ordered, you must define a FilterRegistrationBean for the Filter and set the registration bean’s order using the setOrder(int) method. Avoid configuring a filter that reads the request body at Ordered.HIGHEST_PRECEDENCE, since it might go against the character encoding configuration of your application. If a servlet filter wraps the request, it should be configured with an order that is less than or equal to OrderedFilter.REQUEST_WRAPPER_FILTER_MAX_ORDER.
	To see the order of every Filter in your application, enable debug level logging for the web logging group (logging.level.web=debug). Details of the registered filters, including their order and URL patterns, will then be logged at startup.
	Take care when registering Filter beans since they are initialized very early in the application lifecycle. If you need to register a Filter that interacts with other beans, consider using a DelegatingFilterProxyRegistrationBean instead.
1.3.2. Servlet Context Initialization

Embedded servlet containers do not directly execute the jakarta.servlet.ServletContainerInitializer interface or Spring’s org.springframework.web.WebApplicationInitializer interface. This is an intentional design decision intended to reduce the risk that third party libraries designed to run inside a war may break Spring Boot applications.

If you need to perform servlet context initialization in a Spring Boot application, you should register a bean that implements the org.springframework.boot.web.servlet.ServletContextInitializer interface. The single onStartup method provides access to the ServletContext and, if necessary, can easily be used as an adapter to an existing WebApplicationInitializer.
Scanning for Servlets, Filters, and listeners

When using an embedded container, automatic registration of classes annotated with @WebServlet, @WebFilter, and @WebListener can be enabled by using @ServletComponentScan.
	@ServletComponentScan has no effect in a standalone container, where the container’s built-in discovery mechanisms are used instead.
1.3.3. The ServletWebServerApplicationContext

Under the hood, Spring Boot uses a different type of ApplicationContext for embedded servlet container support. The ServletWebServerApplicationContext is a special type of WebApplicationContext that bootstraps itself by searching for a single ServletWebServerFactory bean. Usually a TomcatServletWebServerFactory, JettyServletWebServerFactory, or UndertowServletWebServerFactory has been auto-configured.
	You usually do not need to be aware of these implementation classes. Most applications are auto-configured, and the appropriate ApplicationContext and ServletWebServerFactory are created on your behalf.

In an embedded container setup, the ServletContext is set as part of server startup which happens during application context initialization. Because of this beans in the ApplicationContext cannot be reliably initialized with a ServletContext. One way to get around this is to inject ApplicationContext as a dependency of the bean and access the ServletContext only when it is needed. Another way is to use a callback once the server has started. This can be done using an ApplicationListener which listens for the ApplicationStartedEvent as follows:

import jakarta.servlet.ServletContext;

import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.web.context.WebApplicationContext;

public class MyDemoBean implements ApplicationListener<ApplicationStartedEvent> {

    private ServletContext servletContext;

    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        ApplicationContext applicationContext = event.getApplicationContext();
        this.servletContext = ((WebApplicationContext) applicationContext).getServletContext();
    }

}

1.3.4. Customizing Embedded Servlet Containers

Common servlet container settings can be configured by using Spring Environment properties. Usually, you would define the properties in your application.properties or application.yaml file.

Common server settings include:

    Network settings: Listen port for incoming HTTP requests (server.port), interface address to bind to (server.address), and so on.

    Session settings: Whether the session is persistent (server.servlet.session.persistent), session timeout (server.servlet.session.timeout), location of session data (server.servlet.session.store-dir), and session-cookie configuration (server.servlet.session.cookie.*).

    Error management: Location of the error page (server.error.path) and so on.

    SSL

    HTTP compression

Spring Boot tries as much as possible to expose common settings, but this is not always possible. For those cases, dedicated namespaces offer server-specific customizations (see server.tomcat and server.undertow). For instance, access logs can be configured with specific features of the embedded servlet container.
	See the ServerProperties class for a complete list.
SameSite Cookies

The SameSite cookie attribute can be used by web browsers to control if and how cookies are submitted in cross-site requests. The attribute is particularly relevant for modern web browsers which have started to change the default value that is used when the attribute is missing.

If you want to change the SameSite attribute of your session cookie, you can use the server.servlet.session.cookie.same-site property. This property is supported by auto-configured Tomcat, Jetty and Undertow servers. It is also used to configure Spring Session servlet based SessionRepository beans.

For example, if you want your session cookie to have a SameSite attribute of None, you can add the following to your application.properties or application.yaml file:
Properties
Yaml

server.servlet.session.cookie.same-site=none

If you want to change the SameSite attribute on other cookies added to your HttpServletResponse, you can use a CookieSameSiteSupplier. The CookieSameSiteSupplier is passed a Cookie and may return a SameSite value, or null.

There are a number of convenience factory and filter methods that you can use to quickly match specific cookies. For example, adding the following bean will automatically apply a SameSite of Lax for all cookies with a name that matches the regular expression myapp.*.
Java
Kotlin

import org.springframework.boot.web.servlet.server.CookieSameSiteSupplier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class MySameSiteConfiguration {

    @Bean
    public CookieSameSiteSupplier applicationCookieSameSiteSupplier() {
        return CookieSameSiteSupplier.ofLax().whenHasNameMatching("myapp.*");
    }

}

Character Encoding

The character encoding behavior of the embedded servlet container for request and response handling can be configured using the server.servlet.encoding.* configuration properties.

When a request’s Accept-Language header indicates a locale for the request it will be automatically mapped to a charset by the servlet container. Each container provides default locale to charset mappings and you should verify that they meet your application’s needs. When they do not, use the server.servlet.encoding.mapping configuration property to customize the mappings, as shown in the following example:
Properties
Yaml

server.servlet.encoding.mapping.ko=UTF-8

In the preceding example, the ko (Korean) locale has been mapped to UTF-8. This is equivalent to a <locale-encoding-mapping-list> entry in a web.xml file of a traditional war deployment.
Programmatic Customization

If you need to programmatically configure your embedded servlet container, you can register a Spring bean that implements the WebServerFactoryCustomizer interface. WebServerFactoryCustomizer provides access to the ConfigurableServletWebServerFactory, which includes numerous customization setter methods. The following example shows programmatically setting the port:
Java
Kotlin

import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.stereotype.Component;

@Component
public class MyWebServerFactoryCustomizer implements WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> {

    @Override
    public void customize(ConfigurableServletWebServerFactory server) {
        server.setPort(9000);
    }

}

TomcatServletWebServerFactory, JettyServletWebServerFactory and UndertowServletWebServerFactory are dedicated variants of ConfigurableServletWebServerFactory that have additional customization setter methods for Tomcat, Jetty and Undertow respectively. The following example shows how to customize TomcatServletWebServerFactory that provides access to Tomcat-specific configuration options:
Java
Kotlin

import java.time.Duration;

import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.stereotype.Component;

@Component
public class MyTomcatWebServerFactoryCustomizer implements WebServerFactoryCustomizer<TomcatServletWebServerFactory> {

    @Override
    public void customize(TomcatServletWebServerFactory server) {
        server.addConnectorCustomizers((connector) -> connector.setAsyncTimeout(Duration.ofSeconds(20).toMillis()));
    }

}

Customizing ConfigurableServletWebServerFactory Directly

For more advanced use cases that require you to extend from ServletWebServerFactory, you can expose a bean of such type yourself.

Setters are provided for many configuration options. Several protected method “hooks” are also provided should you need to do something more exotic. See the source code documentation for details.
	Auto-configured customizers are still applied on your custom factory, so use that option carefully.
1.3.5. JSP Limitations

When running a Spring Boot application that uses an embedded servlet container (and is packaged as an executable archive), there are some limitations in the JSP support.

    With Jetty and Tomcat, it should work if you use war packaging. An executable war will work when launched with java -jar, and will also be deployable to any standard container. JSPs are not supported when using an executable jar.

    Undertow does not support JSPs.

    Creating a custom error.jsp page does not override the default view for error handling. Custom error pages should be used instead.

2. Reactive Web Applications

Spring Boot simplifies development of reactive web applications by providing auto-configuration for Spring Webflux.
2.1. The “Spring WebFlux Framework”

Spring WebFlux is the new reactive web framework introduced in Spring Framework 5.0. Unlike Spring MVC, it does not require the servlet API, is fully asynchronous and non-blocking, and implements the Reactive Streams specification through the Reactor project.

Spring WebFlux comes in two flavors: functional and annotation-based. The annotation-based one is quite close to the Spring MVC model, as shown in the following example:
Java
Kotlin

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class MyRestController {

    private final UserRepository userRepository;

    private final CustomerRepository customerRepository;

    public MyRestController(UserRepository userRepository, CustomerRepository customerRepository) {
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
    }

    @GetMapping("/{userId}")
    public Mono<User> getUser(@PathVariable Long userId) {
        return this.userRepository.findById(userId);
    }

    @GetMapping("/{userId}/customers")
    public Flux<Customer> getUserCustomers(@PathVariable Long userId) {
        return this.userRepository.findById(userId).flatMapMany(this.customerRepository::findByUser);
    }

    @DeleteMapping("/{userId}")
    public Mono<Void> deleteUser(@PathVariable Long userId) {
        return this.userRepository.deleteById(userId);
    }

}

WebFlux is part of the Spring Framework and detailed information is available in its reference documentation.

“WebFlux.fn”, the functional variant, separates the routing configuration from the actual handling of the requests, as shown in the following example:
Java
Kotlin

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RequestPredicate;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration(proxyBeanMethods = false)
public class MyRoutingConfiguration {

    private static final RequestPredicate ACCEPT_JSON = accept(MediaType.APPLICATION_JSON);

    @Bean
    public RouterFunction<ServerResponse> monoRouterFunction(MyUserHandler userHandler) {
        return route()
                .GET("/{user}", ACCEPT_JSON, userHandler::getUser)
                .GET("/{user}/customers", ACCEPT_JSON, userHandler::getUserCustomers)
                .DELETE("/{user}", ACCEPT_JSON, userHandler::deleteUser)
                .build();
    }

}

Java
Kotlin

import reactor.core.publisher.Mono;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

@Component
public class MyUserHandler {

    public Mono<ServerResponse> getUser(ServerRequest request) {
        ...
    }

    public Mono<ServerResponse> getUserCustomers(ServerRequest request) {
        ...
    }

    public Mono<ServerResponse> deleteUser(ServerRequest request) {
        ...
    }

}

“WebFlux.fn” is part of the Spring Framework and detailed information is available in its reference documentation.
	You can define as many RouterFunction beans as you like to modularize the definition of the router. Beans can be ordered if you need to apply a precedence.

To get started, add the spring-boot-starter-webflux module to your application.
	Adding both spring-boot-starter-web and spring-boot-starter-webflux modules in your application results in Spring Boot auto-configuring Spring MVC, not WebFlux. This behavior has been chosen because many Spring developers add spring-boot-starter-webflux to their Spring MVC application to use the reactive WebClient. You can still enforce your choice by setting the chosen application type to SpringApplication.setWebApplicationType(WebApplicationType.REACTIVE).
2.1.1. Spring WebFlux Auto-configuration

Spring Boot provides auto-configuration for Spring WebFlux that works well with most applications.

The auto-configuration adds the following features on top of Spring’s defaults:

    Configuring codecs for HttpMessageReader and HttpMessageWriter instances (described later in this document).

    Support for serving static resources, including support for WebJars (described later in this document).

If you want to keep Spring Boot WebFlux features and you want to add additional WebFlux configuration, you can add your own @Configuration class of type WebFluxConfigurer but without @EnableWebFlux.

If you want to take complete control of Spring WebFlux, you can add your own @Configuration annotated with @EnableWebFlux.
2.1.2. Spring WebFlux Conversion Service

If you want to customize the ConversionService used by Spring WebFlux, you can provide a WebFluxConfigurer bean with an addFormatters method.

Conversion can also be customized using the spring.webflux.format.* configuration properties. When not configured, the following defaults are used:
Property 	DateTimeFormatter

spring.webflux.format.date
	

ofLocalizedDate(FormatStyle.SHORT)

spring.webflux.format.time
	

ofLocalizedTime(FormatStyle.SHORT)

spring.webflux.format.date-time
	

ofLocalizedDateTime(FormatStyle.SHORT)
2.1.3. HTTP Codecs with HttpMessageReaders and HttpMessageWriters

Spring WebFlux uses the HttpMessageReader and HttpMessageWriter interfaces to convert HTTP requests and responses. They are configured with CodecConfigurer to have sensible defaults by looking at the libraries available in your classpath.

Spring Boot provides dedicated configuration properties for codecs, spring.codec.*. It also applies further customization by using CodecCustomizer instances. For example, spring.jackson.* configuration keys are applied to the Jackson codec.

If you need to add or customize codecs, you can create a custom CodecCustomizer component, as shown in the following example:
Java
Kotlin

import org.springframework.boot.web.codec.CodecCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.ServerSentEventHttpMessageReader;

@Configuration(proxyBeanMethods = false)
public class MyCodecsConfiguration {

    @Bean
    public CodecCustomizer myCodecCustomizer() {
        return (configurer) -> {
            configurer.registerDefaults(false);
            configurer.customCodecs().register(new ServerSentEventHttpMessageReader());
            // ...
        };
    }

}

You can also leverage Boot’s custom JSON serializers and deserializers.
2.1.4. Static Content

By default, Spring Boot serves static content from a directory called /static (or /public or /resources or /META-INF/resources) in the classpath. It uses the ResourceWebHandler from Spring WebFlux so that you can modify that behavior by adding your own WebFluxConfigurer and overriding the addResourceHandlers method.

By default, resources are mapped on /**, but you can tune that by setting the spring.webflux.static-path-pattern property. For instance, relocating all resources to /resources/** can be achieved as follows:
Properties
Yaml

spring.webflux.static-path-pattern=/resources/**

You can also customize the static resource locations by using spring.web.resources.static-locations. Doing so replaces the default values with a list of directory locations. If you do so, the default welcome page detection switches to your custom locations. So, if there is an index.html in any of your locations on startup, it is the home page of the application.

In addition to the “standard” static resource locations listed earlier, a special case is made for Webjars content. By default, any resources with a path in /webjars/** are served from jar files if they are packaged in the Webjars format. The path can be customized with the spring.webflux.webjars-path-pattern property.
	Spring WebFlux applications do not strictly depend on the servlet API, so they cannot be deployed as war files and do not use the src/main/webapp directory.
2.1.5. Welcome Page

Spring Boot supports both static and templated welcome pages. It first looks for an index.html file in the configured static content locations. If one is not found, it then looks for an index template. If either is found, it is automatically used as the welcome page of the application.

This only acts as a fallback for actual index routes defined by the application. The ordering is defined by the order of HandlerMapping beans which is by default the following:

RouterFunctionMapping
	

Endpoints declared with RouterFunction beans

RequestMappingHandlerMapping
	

Endpoints declared in @Controller beans

RouterFunctionMapping for the Welcome Page
	

The welcome page support
2.1.6. Template Engines

As well as REST web services, you can also use Spring WebFlux to serve dynamic HTML content. Spring WebFlux supports a variety of templating technologies, including Thymeleaf, FreeMarker, and Mustache.

Spring Boot includes auto-configuration support for the following templating engines:

    FreeMarker

    Thymeleaf

    Mustache

When you use one of these templating engines with the default configuration, your templates are picked up automatically from src/main/resources/templates.
2.1.7. Error Handling

Spring Boot provides a WebExceptionHandler that handles all errors in a sensible way. Its position in the processing order is immediately before the handlers provided by WebFlux, which are considered last. For machine clients, it produces a JSON response with details of the error, the HTTP status, and the exception message. For browser clients, there is a “whitelabel” error handler that renders the same data in HTML format. You can also provide your own HTML templates to display errors (see the next section).

Before customizing error handling in Spring Boot directly, you can leverage the RFC 7807 Problem Details support in Spring WebFlux. Spring WebFlux can produce custom error messages with the application/problem+json media type, like:

{
  "type": "https://example.org/problems/unknown-project",
  "title": "Unknown project",
  "status": 404,
  "detail": "No project found for id 'spring-unknown'",
  "instance": "/projects/spring-unknown"
}

This support can be enabled by setting spring.webflux.problemdetails.enabled to true.

The first step to customizing this feature often involves using the existing mechanism but replacing or augmenting the error contents. For that, you can add a bean of type ErrorAttributes.

To change the error handling behavior, you can implement ErrorWebExceptionHandler and register a bean definition of that type. Because an ErrorWebExceptionHandler is quite low-level, Spring Boot also provides a convenient AbstractErrorWebExceptionHandler to let you handle errors in a WebFlux functional way, as shown in the following example:
Java
Kotlin

import reactor.core.publisher.Mono;

import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.function.server.ServerResponse.BodyBuilder;

@Component
public class MyErrorWebExceptionHandler extends AbstractErrorWebExceptionHandler {

    public MyErrorWebExceptionHandler(ErrorAttributes errorAttributes, WebProperties webProperties,
            ApplicationContext applicationContext, ServerCodecConfigurer serverCodecConfigurer) {
        super(errorAttributes, webProperties.getResources(), applicationContext);
        setMessageReaders(serverCodecConfigurer.getReaders());
        setMessageWriters(serverCodecConfigurer.getWriters());
    }

    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
        return RouterFunctions.route(this::acceptsXml, this::handleErrorAsXml);
    }

    private boolean acceptsXml(ServerRequest request) {
        return request.headers().accept().contains(MediaType.APPLICATION_XML);
    }

    public Mono<ServerResponse> handleErrorAsXml(ServerRequest request) {
        BodyBuilder builder = ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR);
        // ... additional builder calls
        return builder.build();
    }

}

For a more complete picture, you can also subclass DefaultErrorWebExceptionHandler directly and override specific methods.

In some cases, errors handled at the controller level are not recorded by web observations or the metrics infrastructure. Applications can ensure that such exceptions are recorded with the observations by setting the handled exception on the observation context.
Custom Error Pages

If you want to display a custom HTML error page for a given status code, you can add views that resolve from error/*, for example by adding files to a /error directory. Error pages can either be static HTML (that is, added under any of the static resource directories) or built with templates. The name of the file should be the exact status code, a status code series mask, or error for a default if nothing else matches. Note that the path to the default error view is error/error, whereas with Spring MVC the default error view is error.

For example, to map 404 to a static HTML file, your directory structure would be as follows:

src/
 +- main/
     +- java/
     |   + <source code>
     +- resources/
         +- public/
             +- error/
             |   +- 404.html
             +- <other public assets>

To map all 5xx errors by using a Mustache template, your directory structure would be as follows:

src/
 +- main/
     +- java/
     |   + <source code>
     +- resources/
         +- templates/
             +- error/
             |   +- 5xx.mustache
             +- <other templates>

2.1.8. Web Filters

Spring WebFlux provides a WebFilter interface that can be implemented to filter HTTP request-response exchanges. WebFilter beans found in the application context will be automatically used to filter each exchange.

Where the order of the filters is important they can implement Ordered or be annotated with @Order. Spring Boot auto-configuration may configure web filters for you. When it does so, the orders shown in the following table will be used:
Web Filter 	Order

WebFilterChainProxy (Spring Security)
	

-100

HttpExchangesWebFilter
	

Ordered.LOWEST_PRECEDENCE - 10
2.2. Embedded Reactive Server Support

Spring Boot includes support for the following embedded reactive web servers: Reactor Netty, Tomcat, Jetty, and Undertow. Most developers use the appropriate “Starter” to obtain a fully configured instance. By default, the embedded server listens for HTTP requests on port 8080.
2.2.1. Customizing Reactive Servers

Common reactive web server settings can be configured by using Spring Environment properties. Usually, you would define the properties in your application.properties or application.yaml file.

Common server settings include:

    Network settings: Listen port for incoming HTTP requests (server.port), interface address to bind to (server.address), and so on.

    Error management: Location of the error page (server.error.path) and so on.

    SSL

    HTTP compression

Spring Boot tries as much as possible to expose common settings, but this is not always possible. For those cases, dedicated namespaces such as server.netty.* offer server-specific customizations.
	See the ServerProperties class for a complete list.
Programmatic Customization

If you need to programmatically configure your reactive web server, you can register a Spring bean that implements the WebServerFactoryCustomizer interface. WebServerFactoryCustomizer provides access to the ConfigurableReactiveWebServerFactory, which includes numerous customization setter methods. The following example shows programmatically setting the port:
Java
Kotlin

import org.springframework.boot.web.reactive.server.ConfigurableReactiveWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.stereotype.Component;

@Component
public class MyWebServerFactoryCustomizer implements WebServerFactoryCustomizer<ConfigurableReactiveWebServerFactory> {

    @Override
    public void customize(ConfigurableReactiveWebServerFactory server) {
        server.setPort(9000);
    }

}

JettyReactiveWebServerFactory, NettyReactiveWebServerFactory, TomcatReactiveWebServerFactory, and UndertowReactiveWebServerFactory are dedicated variants of ConfigurableReactiveWebServerFactory that have additional customization setter methods for Jetty, Reactor Netty, Tomcat, and Undertow respectively. The following example shows how to customize NettyReactiveWebServerFactory that provides access to Reactor Netty-specific configuration options:
Java
Kotlin

import java.time.Duration;

import org.springframework.boot.web.embedded.netty.NettyReactiveWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.stereotype.Component;

@Component
public class MyNettyWebServerFactoryCustomizer implements WebServerFactoryCustomizer<NettyReactiveWebServerFactory> {

    @Override
    public void customize(NettyReactiveWebServerFactory factory) {
        factory.addServerCustomizers((server) -> server.idleTimeout(Duration.ofSeconds(20)));
    }

}

Customizing ConfigurableReactiveWebServerFactory Directly

For more advanced use cases that require you to extend from ReactiveWebServerFactory, you can expose a bean of such type yourself.

Setters are provided for many configuration options. Several protected method “hooks” are also provided should you need to do something more exotic. See the source code documentation for details.
	Auto-configured customizers are still applied on your custom factory, so use that option carefully.
2.3. Reactive Server Resources Configuration

When auto-configuring a Reactor Netty or Jetty server, Spring Boot will create specific beans that will provide HTTP resources to the server instance: ReactorResourceFactory or JettyResourceFactory.

By default, those resources will be also shared with the Reactor Netty and Jetty clients for optimal performances, given:

    the same technology is used for server and client

    the client instance is built using the WebClient.Builder bean auto-configured by Spring Boot

Developers can override the resource configuration for Jetty and Reactor Netty by providing a custom ReactorResourceFactory or JettyResourceFactory bean - this will be applied to both clients and servers.

You can learn more about the resource configuration on the client side in the WebClient Runtime section.
3. Graceful Shutdown

Graceful shutdown is supported with all four embedded web servers (Jetty, Reactor Netty, Tomcat, and Undertow) and with both reactive and servlet-based web applications. It occurs as part of closing the application context and is performed in the earliest phase of stopping SmartLifecycle beans. This stop processing uses a timeout which provides a grace period during which existing requests will be allowed to complete but no new requests will be permitted. The exact way in which new requests are not permitted varies depending on the web server that is being used. Jetty, Reactor Netty, and Tomcat will stop accepting requests at the network layer. Undertow will accept requests but respond immediately with a service unavailable (503) response.
	Graceful shutdown with Tomcat requires Tomcat 9.0.33 or later.

To enable graceful shutdown, configure the server.shutdown property, as shown in the following example:
Properties
Yaml

server.shutdown=graceful

To configure the timeout period, configure the spring.lifecycle.timeout-per-shutdown-phase property, as shown in the following example:
Properties
Yaml

spring.lifecycle.timeout-per-shutdown-phase=20s

	Using graceful shutdown with your IDE may not work properly if it does not send a proper SIGTERM signal. See the documentation of your IDE for more details.
4. Spring Security

If Spring Security is on the classpath, then web applications are secured by default. Spring Boot relies on Spring Security’s content-negotiation strategy to determine whether to use httpBasic or formLogin. To add method-level security to a web application, you can also add @EnableGlobalMethodSecurity with your desired settings. Additional information can be found in the Spring Security Reference Guide.

The default UserDetailsService has a single user. The user name is user, and the password is random and is printed at WARN level when the application starts, as shown in the following example:

Using generated security password: 78fa095d-3f4c-48b1-ad50-e24c31d5cf35

This generated password is for development use only. Your security configuration must be updated before running your application in production.

	If you fine-tune your logging configuration, ensure that the org.springframework.boot.autoconfigure.security category is set to log WARN-level messages. Otherwise, the default password is not printed.

You can change the username and password by providing a spring.security.user.name and spring.security.user.password.

The basic features you get by default in a web application are:

    A UserDetailsService (or ReactiveUserDetailsService in case of a WebFlux application) bean with in-memory store and a single user with a generated password (see SecurityProperties.User for the properties of the user).

    Form-based login or HTTP Basic security (depending on the Accept header in the request) for the entire application (including actuator endpoints if actuator is on the classpath).

    A DefaultAuthenticationEventPublisher for publishing authentication events.

You can provide a different AuthenticationEventPublisher by adding a bean for it.
4.1. MVC Security

The default security configuration is implemented in SecurityAutoConfiguration and UserDetailsServiceAutoConfiguration. SecurityAutoConfiguration imports SpringBootWebSecurityConfiguration for web security and UserDetailsServiceAutoConfiguration configures authentication, which is also relevant in non-web applications.

To switch off the default web application security configuration completely or to combine multiple Spring Security components such as OAuth2 Client and Resource Server, add a bean of type SecurityFilterChain (doing so does not disable the UserDetailsService configuration or Actuator’s security). To also switch off the UserDetailsService configuration, you can add a bean of type UserDetailsService, AuthenticationProvider, or AuthenticationManager.

The auto-configuration of a UserDetailsService will also back off any of the following Spring Security modules is on the classpath:

    spring-security-oauth2-client

    spring-security-oauth2-resource-server

    spring-security-saml2-service-provider

To use UserDetailsService in addition to one or more of these dependencies, define your own InMemoryUserDetailsManager bean.

Access rules can be overridden by adding a custom SecurityFilterChain bean. Spring Boot provides convenience methods that can be used to override access rules for actuator endpoints and static resources. EndpointRequest can be used to create a RequestMatcher that is based on the management.endpoints.web.base-path property. PathRequest can be used to create a RequestMatcher for resources in commonly used locations.
4.2. WebFlux Security

Similar to Spring MVC applications, you can secure your WebFlux applications by adding the spring-boot-starter-security dependency. The default security configuration is implemented in ReactiveSecurityAutoConfiguration and UserDetailsServiceAutoConfiguration. ReactiveSecurityAutoConfiguration imports WebFluxSecurityConfiguration for web security and UserDetailsServiceAutoConfiguration configures authentication, which is also relevant in non-web applications.

To switch off the default web application security configuration completely, you can add a bean of type WebFilterChainProxy (doing so does not disable the UserDetailsService configuration or Actuator’s security). To also switch off the UserDetailsService configuration, you can add a bean of type ReactiveUserDetailsService or ReactiveAuthenticationManager.

The auto-configuration will also back off when any of the following Spring Security modules is on the classpath:

    spring-security-oauth2-client

    spring-security-oauth2-resource-server

To use ReactiveUserDetailsService in addition to one or more of these dependencies, define your own MapReactiveUserDetailsService bean.

Access rules and the use of multiple Spring Security components such as OAuth 2 Client and Resource Server can be configured by adding a custom SecurityWebFilterChain bean. Spring Boot provides convenience methods that can be used to override access rules for actuator endpoints and static resources. EndpointRequest can be used to create a ServerWebExchangeMatcher that is based on the management.endpoints.web.base-path property.

PathRequest can be used to create a ServerWebExchangeMatcher for resources in commonly used locations.

For example, you can customize your security configuration by adding something like:
Java
Kotlin

import org.springframework.boot.autoconfigure.security.reactive.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration(proxyBeanMethods = false)
public class MyWebFluxSecurityConfiguration {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http.authorizeExchange((exchange) -> {
            exchange.matchers(PathRequest.toStaticResources().atCommonLocations()).permitAll();
            exchange.pathMatchers("/foo", "/bar").authenticated();
        });
        http.formLogin(withDefaults());
        return http.build();
    }

}

4.3. OAuth2

OAuth2 is a widely used authorization framework that is supported by Spring.
4.3.1. Client

If you have spring-security-oauth2-client on your classpath, you can take advantage of some auto-configuration to set up OAuth2/Open ID Connect clients. This configuration makes use of the properties under OAuth2ClientProperties. The same properties are applicable to both servlet and reactive applications.

You can register multiple OAuth2 clients and providers under the spring.security.oauth2.client prefix, as shown in the following example:
Properties
Yaml

spring.security.oauth2.client.registration.my-login-client.client-id=abcd
spring.security.oauth2.client.registration.my-login-client.client-secret=password
spring.security.oauth2.client.registration.my-login-client.client-name=Client for OpenID Connect
spring.security.oauth2.client.registration.my-login-client.provider=my-oauth-provider
spring.security.oauth2.client.registration.my-login-client.scope=openid,profile,email,phone,address
spring.security.oauth2.client.registration.my-login-client.redirect-uri={baseUrl}/login/oauth2/code/{registrationId}
spring.security.oauth2.client.registration.my-login-client.client-authentication-method=client_secret_basic
spring.security.oauth2.client.registration.my-login-client.authorization-grant-type=authorization_code

spring.security.oauth2.client.registration.my-client-1.client-id=abcd
spring.security.oauth2.client.registration.my-client-1.client-secret=password
spring.security.oauth2.client.registration.my-client-1.client-name=Client for user scope
spring.security.oauth2.client.registration.my-client-1.provider=my-oauth-provider
spring.security.oauth2.client.registration.my-client-1.scope=user
spring.security.oauth2.client.registration.my-client-1.redirect-uri={baseUrl}/authorized/user
spring.security.oauth2.client.registration.my-client-1.client-authentication-method=client_secret_basic
spring.security.oauth2.client.registration.my-client-1.authorization-grant-type=authorization_code

spring.security.oauth2.client.registration.my-client-2.client-id=abcd
spring.security.oauth2.client.registration.my-client-2.client-secret=password
spring.security.oauth2.client.registration.my-client-2.client-name=Client for email scope
spring.security.oauth2.client.registration.my-client-2.provider=my-oauth-provider
spring.security.oauth2.client.registration.my-client-2.scope=email
spring.security.oauth2.client.registration.my-client-2.redirect-uri={baseUrl}/authorized/email
spring.security.oauth2.client.registration.my-client-2.client-authentication-method=client_secret_basic
spring.security.oauth2.client.registration.my-client-2.authorization-grant-type=authorization_code

spring.security.oauth2.client.provider.my-oauth-provider.authorization-uri=https://my-auth-server.com/oauth2/authorize
spring.security.oauth2.client.provider.my-oauth-provider.token-uri=https://my-auth-server.com/oauth2/token
spring.security.oauth2.client.provider.my-oauth-provider.user-info-uri=https://my-auth-server.com/userinfo
spring.security.oauth2.client.provider.my-oauth-provider.user-info-authentication-method=header
spring.security.oauth2.client.provider.my-oauth-provider.jwk-set-uri=https://my-auth-server.com/oauth2/jwks
spring.security.oauth2.client.provider.my-oauth-provider.user-name-attribute=name

For OpenID Connect providers that support OpenID Connect discovery, the configuration can be further simplified. The provider needs to be configured with an issuer-uri which is the URI that it asserts as its Issuer Identifier. For example, if the issuer-uri provided is "https://example.com", then an "OpenID Provider Configuration Request" will be made to "https://example.com/.well-known/openid-configuration". The result is expected to be an "OpenID Provider Configuration Response". The following example shows how an OpenID Connect Provider can be configured with the issuer-uri:
Properties
Yaml

spring.security.oauth2.client.provider.oidc-provider.issuer-uri=https://dev-123456.oktapreview.com/oauth2/default/

By default, Spring Security’s OAuth2LoginAuthenticationFilter only processes URLs matching /login/oauth2/code/*. If you want to customize the redirect-uri to use a different pattern, you need to provide configuration to process that custom pattern. For example, for servlet applications, you can add your own SecurityFilterChain that resembles the following:
Java
Kotlin

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration(proxyBeanMethods = false)
@EnableWebSecurity
public class MyOAuthClientConfiguration {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests((requests) -> requests
                .anyRequest().authenticated()
            )
            .oauth2Login((login) -> login
                .redirectionEndpoint((endpoint) -> endpoint
                    .baseUri("/login/oauth2/callback/*")
                )
            );
        return http.build();
    }

}

	Spring Boot auto-configures an InMemoryOAuth2AuthorizedClientService which is used by Spring Security for the management of client registrations. The InMemoryOAuth2AuthorizedClientService has limited capabilities and we recommend using it only for development environments. For production environments, consider using a JdbcOAuth2AuthorizedClientService or creating your own implementation of OAuth2AuthorizedClientService.
OAuth2 Client Registration for Common Providers

For common OAuth2 and OpenID providers, including Google, Github, Facebook, and Okta, we provide a set of provider defaults (google, github, facebook, and okta, respectively).

If you do not need to customize these providers, you can set the provider attribute to the one for which you need to infer defaults. Also, if the key for the client registration matches a default supported provider, Spring Boot infers that as well.

In other words, the two configurations in the following example use the Google provider:
Properties
Yaml

spring.security.oauth2.client.registration.my-client.client-id=abcd
spring.security.oauth2.client.registration.my-client.client-secret=password
spring.security.oauth2.client.registration.my-client.provider=google
spring.security.oauth2.client.registration.google.client-id=abcd
spring.security.oauth2.client.registration.google.client-secret=password

4.3.2. Resource Server

If you have spring-security-oauth2-resource-server on your classpath, Spring Boot can set up an OAuth2 Resource Server. For JWT configuration, a JWK Set URI or OIDC Issuer URI needs to be specified, as shown in the following examples:
Properties
Yaml

spring.security.oauth2.resourceserver.jwt.jwk-set-uri=https://example.com/oauth2/default/v1/keys

Properties
Yaml

spring.security.oauth2.resourceserver.jwt.issuer-uri=https://dev-123456.oktapreview.com/oauth2/default/

	If the authorization server does not support a JWK Set URI, you can configure the resource server with the Public Key used for verifying the signature of the JWT. This can be done using the spring.security.oauth2.resourceserver.jwt.public-key-location property, where the value needs to point to a file containing the public key in the PEM-encoded x509 format.

The spring.security.oauth2.resourceserver.jwt.audiences property can be used to specify the expected values of the aud claim in JWTs. For example, to require JWTs to contain an aud claim with the value my-audience:
Properties
Yaml

spring.security.oauth2.resourceserver.jwt.audiences[0]=my-audience

The same properties are applicable for both servlet and reactive applications. Alternatively, you can define your own JwtDecoder bean for servlet applications or a ReactiveJwtDecoder for reactive applications.

In cases where opaque tokens are used instead of JWTs, you can configure the following properties to validate tokens through introspection:
Properties
Yaml

spring.security.oauth2.resourceserver.opaquetoken.introspection-uri=https://example.com/check-token
spring.security.oauth2.resourceserver.opaquetoken.client-id=my-client-id
spring.security.oauth2.resourceserver.opaquetoken.client-secret=my-client-secret

Again, the same properties are applicable for both servlet and reactive applications. Alternatively, you can define your own OpaqueTokenIntrospector bean for servlet applications or a ReactiveOpaqueTokenIntrospector for reactive applications.
4.3.3. Authorization Server

If you have spring-security-oauth2-authorization-server on your classpath, you can take advantage of some auto-configuration to set up a Servlet-based OAuth2 Authorization Server.

You can register multiple OAuth2 clients under the spring.security.oauth2.authorizationserver.client prefix, as shown in the following example:
Properties
Yaml

spring.security.oauth2.authorizationserver.client.my-client-1.registration.client-id=abcd
spring.security.oauth2.authorizationserver.client.my-client-1.registration.client-secret={noop}secret1
spring.security.oauth2.authorizationserver.client.my-client-1.registration.client-authentication-methods[0]=client_secret_basic
spring.security.oauth2.authorizationserver.client.my-client-1.registration.authorization-grant-types[0]=authorization_code
spring.security.oauth2.authorizationserver.client.my-client-1.registration.authorization-grant-types[1]=refresh_token
spring.security.oauth2.authorizationserver.client.my-client-1.registration.redirect-uris[0]=https://my-client-1.com/login/oauth2/code/abcd
spring.security.oauth2.authorizationserver.client.my-client-1.registration.redirect-uris[1]=https://my-client-1.com/authorized
spring.security.oauth2.authorizationserver.client.my-client-1.registration.scopes[0]=openid
spring.security.oauth2.authorizationserver.client.my-client-1.registration.scopes[1]=profile
spring.security.oauth2.authorizationserver.client.my-client-1.registration.scopes[2]=email
spring.security.oauth2.authorizationserver.client.my-client-1.registration.scopes[3]=phone
spring.security.oauth2.authorizationserver.client.my-client-1.registration.scopes[4]=address
spring.security.oauth2.authorizationserver.client.my-client-1.require-authorization-consent=true
spring.security.oauth2.authorizationserver.client.my-client-2.registration.client-id=efgh
spring.security.oauth2.authorizationserver.client.my-client-2.registration.client-secret={noop}secret2
spring.security.oauth2.authorizationserver.client.my-client-2.registration.client-authentication-methods[0]=client_secret_jwt
spring.security.oauth2.authorizationserver.client.my-client-2.registration.authorization-grant-types[0]=client_credentials
spring.security.oauth2.authorizationserver.client.my-client-2.registration.scopes[0]=user.read
spring.security.oauth2.authorizationserver.client.my-client-2.registration.scopes[1]=user.write
spring.security.oauth2.authorizationserver.client.my-client-2.jwk-set-uri=https://my-client-2.com/jwks
spring.security.oauth2.authorizationserver.client.my-client-2.token-endpoint-authentication-signing-algorithm=RS256

	The client-secret property must be in a format that can be matched by the configured PasswordEncoder. The default instance of PasswordEncoder is created via PasswordEncoderFactories.createDelegatingPasswordEncoder().

The auto-configuration Spring Boot provides for Spring Authorization Server is designed for getting started quickly. Most applications will require customization and will want to define several beans to override auto-configuration.

The following components can be defined as beans to override auto-configuration specific to Spring Authorization Server:

    RegisteredClientRepository

    AuthorizationServerSettings

    SecurityFilterChain

    com.nimbusds.jose.jwk.source.JWKSource<com.nimbusds.jose.proc.SecurityContext>

    JwtDecoder

	Spring Boot auto-configures an InMemoryRegisteredClientRepository which is used by Spring Authorization Server for the management of registered clients. The InMemoryRegisteredClientRepository has limited capabilities and we recommend using it only for development environments. For production environments, consider using a JdbcRegisteredClientRepository or creating your own implementation of RegisteredClientRepository.

Additional information can be found in the Getting Started chapter of the Spring Authorization Server Reference Guide.
4.4. SAML 2.0
4.4.1. Relying Party

If you have spring-security-saml2-service-provider on your classpath, you can take advantage of some auto-configuration to set up a SAML 2.0 Relying Party. This configuration makes use of the properties under Saml2RelyingPartyProperties.

A relying party registration represents a paired configuration between an Identity Provider, IDP, and a Service Provider, SP. You can register multiple relying parties under the spring.security.saml2.relyingparty prefix, as shown in the following example:
Properties
Yaml

spring.security.saml2.relyingparty.registration.my-relying-party1.signing.credentials[0].private-key-location=path-to-private-key
spring.security.saml2.relyingparty.registration.my-relying-party1.signing.credentials[0].certificate-location=path-to-certificate
spring.security.saml2.relyingparty.registration.my-relying-party1.decryption.credentials[0].private-key-location=path-to-private-key
spring.security.saml2.relyingparty.registration.my-relying-party1.decryption.credentials[0].certificate-location=path-to-certificate
spring.security.saml2.relyingparty.registration.my-relying-party1.singlelogout.url=https://myapp/logout/saml2/slo
spring.security.saml2.relyingparty.registration.my-relying-party1.singlelogout.response-url=https://remoteidp2.slo.url
spring.security.saml2.relyingparty.registration.my-relying-party1.singlelogout.binding=POST
spring.security.saml2.relyingparty.registration.my-relying-party1.assertingparty.verification.credentials[0].certificate-location=path-to-verification-cert
spring.security.saml2.relyingparty.registration.my-relying-party1.assertingparty.entity-id=remote-idp-entity-id1
spring.security.saml2.relyingparty.registration.my-relying-party1.assertingparty.sso-url=https://remoteidp1.sso.url

spring.security.saml2.relyingparty.registration.my-relying-party2.signing.credentials[0].private-key-location=path-to-private-key
spring.security.saml2.relyingparty.registration.my-relying-party2.signing.credentials[0].certificate-location=path-to-certificate
spring.security.saml2.relyingparty.registration.my-relying-party2.decryption.credentials[0].private-key-location=path-to-private-key
spring.security.saml2.relyingparty.registration.my-relying-party2.decryption.credentials[0].certificate-location=path-to-certificate
spring.security.saml2.relyingparty.registration.my-relying-party2.assertingparty.verification.credentials[0].certificate-location=path-to-other-verification-cert
spring.security.saml2.relyingparty.registration.my-relying-party2.assertingparty.entity-id=remote-idp-entity-id2
spring.security.saml2.relyingparty.registration.my-relying-party2.assertingparty.sso-url=https://remoteidp2.sso.url
spring.security.saml2.relyingparty.registration.my-relying-party2.assertingparty.singlelogout.url=https://remoteidp2.slo.url
spring.security.saml2.relyingparty.registration.my-relying-party2.assertingparty.singlelogout.response-url=https://myapp/logout/saml2/slo
spring.security.saml2.relyingparty.registration.my-relying-party2.assertingparty.singlelogout.binding=POST

For SAML2 logout, by default, Spring Security’s Saml2LogoutRequestFilter and Saml2LogoutResponseFilter only process URLs matching /logout/saml2/slo. If you want to customize the url to which AP-initiated logout requests get sent to or the response-url to which an AP sends logout responses to, to use a different pattern, you need to provide configuration to process that custom pattern. For example, for servlet applications, you can add your own SecurityFilterChain that resembles the following:

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration(proxyBeanMethods = false)
public class MySamlRelyingPartyConfiguration {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests((requests) -> requests.anyRequest().authenticated());
        http.saml2Login(withDefaults());
        http.saml2Logout((saml2) -> saml2.logoutRequest((request) -> request.logoutUrl("/SLOService.saml2"))
            .logoutResponse((response) -> response.logoutUrl("/SLOService.saml2")));
        return http.build();
    }

}

5. Spring Session

Spring Boot provides Spring Session auto-configuration for a wide range of data stores. When building a servlet web application, the following stores can be auto-configured:

    Redis

    JDBC

    Hazelcast

    MongoDB

Additionally, Spring Boot for Apache Geode provides auto-configuration for using Apache Geode as a session store.

The servlet auto-configuration replaces the need to use @Enable*HttpSession.

If a single Spring Session module is present on the classpath, Spring Boot uses that store implementation automatically. If you have more than one implementation, Spring Boot uses the following order for choosing a specific implementation:

    Redis

    JDBC

    Hazelcast

    MongoDB

    If none of Redis, JDBC, Hazelcast and MongoDB are available, we do not configure a SessionRepository.

When building a reactive web application, the following stores can be auto-configured:

    Redis

    MongoDB

The reactive auto-configuration replaces the need to use @Enable*WebSession.

Similar to the servlet configuration, if you have more than one implementation, Spring Boot uses the following order for choosing a specific implementation:

    Redis

    MongoDB

    If neither Redis nor MongoDB are available, we do not configure a ReactiveSessionRepository.

Each store has specific additional settings. For instance, it is possible to customize the name of the table for the JDBC store, as shown in the following example:
Properties
Yaml

spring.session.jdbc.table-name=SESSIONS

For setting the timeout of the session you can use the spring.session.timeout property. If that property is not set with a servlet web application, the auto-configuration falls back to the value of server.servlet.session.timeout.

You can take control over Spring Session’s configuration using @Enable*HttpSession (servlet) or @Enable*WebSession (reactive). This will cause the auto-configuration to back off. Spring Session can then be configured using the annotation’s attributes rather than the previously described configuration properties.
6. Spring for GraphQL

If you want to build GraphQL applications, you can take advantage of Spring Boot’s auto-configuration for Spring for GraphQL. The Spring for GraphQL project is based on GraphQL Java. You’ll need the spring-boot-starter-graphql starter at a minimum. Because GraphQL is transport-agnostic, you’ll also need to have one or more additional starters in your application to expose your GraphQL API over the web:
Starter 	Transport 	Implementation

spring-boot-starter-web
	

HTTP
	

Spring MVC

spring-boot-starter-websocket
	

WebSocket
	

WebSocket for Servlet apps

spring-boot-starter-webflux
	

HTTP, WebSocket
	

Spring WebFlux

spring-boot-starter-rsocket
	

TCP, WebSocket
	

Spring WebFlux on Reactor Netty
6.1. GraphQL Schema

A Spring GraphQL application requires a defined schema at startup. By default, you can write ".graphqls" or ".gqls" schema files under src/main/resources/graphql/** and Spring Boot will pick them up automatically. You can customize the locations with spring.graphql.schema.locations and the file extensions with spring.graphql.schema.file-extensions.
	If you want Spring Boot to detect schema files in all your application modules and dependencies for that location, you can set spring.graphql.schema.locations to "classpath*:graphql/**/" (note the classpath*: prefix).

In the following sections, we’ll consider this sample GraphQL schema, defining two types and two queries:

type Query {
    greeting(name: String! = "Spring"): String!
    project(slug: ID!): Project
}

""" A Project in the Spring portfolio """
type Project {
    """ Unique string id used in URLs """
    slug: ID!
    """ Project name """
    name: String!
    """ URL of the git repository """
    repositoryUrl: String!
    """ Current support status """
    status: ProjectStatus!
}

enum ProjectStatus {
    """ Actively supported by the Spring team """
    ACTIVE
    """ Supported by the community """
    COMMUNITY
    """ Prototype, not officially supported yet  """
    INCUBATING
    """ Project being retired, in maintenance mode """
    ATTIC
    """ End-Of-Lifed """
    EOL
}

	By default, field introspection will be allowed on the schema as it is required for tools such as GraphiQL. If you wish to not expose information about the schema, you can disable introspection by setting spring.graphql.schema.introspection.enabled to false.
6.2. GraphQL RuntimeWiring

The GraphQL Java RuntimeWiring.Builder can be used to register custom scalar types, directives, type resolvers, DataFetcher, and more. You can declare RuntimeWiringConfigurer beans in your Spring config to get access to the RuntimeWiring.Builder. Spring Boot detects such beans and adds them to the GraphQlSource builder.

Typically, however, applications will not implement DataFetcher directly and will instead create annotated controllers. Spring Boot will automatically detect @Controller classes with annotated handler methods and register those as DataFetchers. Here’s a sample implementation for our greeting query with a @Controller class:
Java
Kotlin

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
public class GreetingController {

    @QueryMapping
    public String greeting(@Argument String name) {
        return "Hello, " + name + "!";
    }

}

6.3. Querydsl and QueryByExample Repositories Support

Spring Data offers support for both Querydsl and QueryByExample repositories. Spring GraphQL can configure Querydsl and QueryByExample repositories as DataFetcher.

Spring Data repositories annotated with @GraphQlRepository and extending one of:

    QuerydslPredicateExecutor

    ReactiveQuerydslPredicateExecutor

    QueryByExampleExecutor

    ReactiveQueryByExampleExecutor

are detected by Spring Boot and considered as candidates for DataFetcher for matching top-level queries.
6.4. Transports
6.4.1. HTTP and WebSocket

The GraphQL HTTP endpoint is at HTTP POST /graphql by default. The path can be customized with spring.graphql.path.
	The HTTP endpoint for both Spring MVC and Spring WebFlux is provided by a RouterFunction bean with an @Order of 0. If you define your own RouterFunction beans, you may want to add appropriate @Order annotations to ensure that they are sorted correctly.

The GraphQL WebSocket endpoint is off by default. To enable it:

    For a Servlet application, add the WebSocket starter spring-boot-starter-websocket

    For a WebFlux application, no additional dependency is required

    For both, the spring.graphql.websocket.path application property must be set

Spring GraphQL provides a Web Interception model. This is quite useful for retrieving information from an HTTP request header and set it in the GraphQL context or fetching information from the same context and writing it to a response header. With Spring Boot, you can declare a WebInterceptor bean to have it registered with the web transport.

Spring MVC and Spring WebFlux support CORS (Cross-Origin Resource Sharing) requests. CORS is a critical part of the web config for GraphQL applications that are accessed from browsers using different domains.

Spring Boot supports many configuration properties under the spring.graphql.cors.* namespace; here’s a short configuration sample:
Properties
Yaml

spring.graphql.cors.allowed-origins=https://example.org
spring.graphql.cors.allowed-methods=GET,POST
spring.graphql.cors.max-age=1800s

6.4.2. RSocket

RSocket is also supported as a transport, on top of WebSocket or TCP. Once the RSocket server is configured, we can configure our GraphQL handler on a particular route using spring.graphql.rsocket.mapping. For example, configuring that mapping as "graphql" means we can use that as a route when sending requests with the RSocketGraphQlClient.

Spring Boot auto-configures a RSocketGraphQlClient.Builder<?> bean that you can inject in your components:
Java
Kotlin

@Component
public class RSocketGraphQlClientExample {

    private final RSocketGraphQlClient graphQlClient;

    public RSocketGraphQlClientExample(RSocketGraphQlClient.Builder<?> builder) {
        this.graphQlClient = builder.tcp("example.spring.io", 8181).route("graphql").build();
    }

And then send a request:
Java
Kotlin

Mono<Book> book = this.graphQlClient.document("{ bookById(id: \"book-1\"){ id name pageCount author } }")
    .retrieve("bookById")
    .toEntity(Book.class);

6.5. Exception Handling

Spring GraphQL enables applications to register one or more Spring DataFetcherExceptionResolver components that are invoked sequentially. The Exception must be resolved to a list of graphql.GraphQLError objects, see Spring GraphQL exception handling documentation. Spring Boot will automatically detect DataFetcherExceptionResolver beans and register them with the GraphQlSource.Builder.
6.6. GraphiQL and Schema printer

Spring GraphQL offers infrastructure for helping developers when consuming or developing a GraphQL API.

Spring GraphQL ships with a default GraphiQL page that is exposed at "/graphiql" by default. This page is disabled by default and can be turned on with the spring.graphql.graphiql.enabled property. Many applications exposing such a page will prefer a custom build. A default implementation is very useful during development, this is why it is exposed automatically with spring-boot-devtools during development.

You can also choose to expose the GraphQL schema in text format at /graphql/schema when the spring.graphql.schema.printer.enabled property is enabled.
7. Spring HATEOAS

If you develop a RESTful API that makes use of hypermedia, Spring Boot provides auto-configuration for Spring HATEOAS that works well with most applications. The auto-configuration replaces the need to use @EnableHypermediaSupport and registers a number of beans to ease building hypermedia-based applications, including a LinkDiscoverers (for client side support) and an ObjectMapper configured to correctly marshal responses into the desired representation. The ObjectMapper is customized by setting the various spring.jackson.* properties or, if one exists, by a Jackson2ObjectMapperBuilder bean.

You can take control of Spring HATEOAS’s configuration by using @EnableHypermediaSupport. Note that doing so disables the ObjectMapper customization described earlier.
	spring-boot-starter-hateoas is specific to Spring MVC and should not be combined with Spring WebFlux. In order to use Spring HATEOAS with Spring WebFlux, you can add a direct dependency on org.springframework.hateoas:spring-hateoas along with spring-boot-starter-webflux.

By default, requests that accept application/json will receive an application/hal+json response. To disable this behavior set spring.hateoas.use-hal-as-default-json-media-type to false and define a HypermediaMappingInformation or HalConfiguration to configure Spring HATEOAS to meet the needs of your application and its clients.
