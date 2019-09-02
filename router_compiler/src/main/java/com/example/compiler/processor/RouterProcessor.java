package com.example.compiler.processor;

import com.example.compiler.utils.Consts;
import com.example.compiler.utils.Log;
import com.example.compiler.utils.Utils;
import com.example.router.annotation.Route;
import com.example.router.annotation.model.RouteMeta;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;


/**
 * Created by yangximin on 2019/8/29.
 */
@AutoService(Processor.class)
@SupportedOptions(Consts.ARGUMENTS_NAME)
@SupportedSourceVersion(SourceVersion.RELEASE_7)
@SupportedAnnotationTypes({Consts.ANN_TYPE_ROUTE})
public class RouterProcessor extends AbstractProcessor {
    /**
     * key 组名 value 类名
     */
    private Map<String, String> rootMap = new TreeMap<>();
    //分组key：组名  value：对应组的路由信息
    private Map<String, List<RouteMeta>> groupMap = new HashMap<>();
    //节点工具 类，函数 属性都是节点
    private Elements elementUtils;
    //type类信息工具类
    private Types typeUtils;
    //文件生成器
    private Filer filerUtils;
    //参数
    private String moduleName;
    private Log log;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        log = Log.newLog(processingEnvironment.getMessager());
        elementUtils = processingEnvironment.getElementUtils();
        typeUtils = processingEnvironment.getTypeUtils();
        filerUtils = processingEnvironment.getFiler();
        Map<String, String> options = processingEnvironment.getOptions();
        if (!Utils.isEmpty(options)) {
            moduleName = options.get(Consts.ARGUMENTS_NAME);
        }
        log.i("RouterProcessor Parmters:" + moduleName);
        if (Utils.isEmpty(moduleName)) {
            throw new RuntimeException("Not set Processor Parmaters");
        }

    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        if (!Utils.isEmpty(set)) {
            Set<? extends Element> routerElements = roundEnvironment.getElementsAnnotatedWith((Class<? extends Annotation>) Route.class);
            if (!Utils.isEmpty(routerElements)) {
                processRouter(routerElements);
            }
            return true;
        }
        return false;
    }

    private void processRouter(Set<? extends Element> routerElements) {
        //获得类节点信息
        TypeElement typeElement = elementUtils.getTypeElement(Consts.ACTIVITY);
        for (Element element : routerElements) {
            RouteMeta routeMeta;
            //类信息
            TypeMirror typeMirror = element.asType();
            log.i("Route class" + typeMirror.toString());
            Route route = element.getAnnotation(Route.class);
            //只能在指定的类上面使用
            if (typeUtils.isSubtype(typeMirror, typeElement.asType())) {
                routeMeta = new RouteMeta(RouteMeta.Type.ACTIVITY, route, element);

            } else {
                throw new RuntimeException("Just support activity Router" + element);
            }
            //创建group
            categories(routeMeta);
        }
        //groupMap
        TypeElement iRouterGroup = elementUtils.getTypeElement(Consts.IROUTE_GROUP);
        //组集合表
        TypeElement iRouterRoot = elementUtils.getTypeElement(Consts.IROUTE_ROOT);

        //生成 $$Group$$ 记录分组表
        generateGroup(iRouterGroup);

        //生成 $$Root$$ 路由表
        generateRoot(iRouterGroup, iRouterRoot);
    }


    private void generateGroup(TypeElement iRouterGroup) {
        //创建参数类型
        ParameterizedTypeName parameterizedType = ParameterizedTypeName.get(ClassName.get(Map.class),
                ClassName.get(String.class),
                ClassName.get(RouteMeta.class));
        //创建参数
        ParameterSpec atlas = ParameterSpec.builder(parameterizedType, "atlas").build();
        //遍历分组 每一个分组创建一个 $$Group$$ 类
        for (Map.Entry<String, List<RouteMeta>> entries : groupMap.entrySet()) {
            //创建函数
            MethodSpec.Builder method = MethodSpec.methodBuilder("loadInto")
                    .addModifiers(Modifier.PUBLIC)
                    .returns(TypeName.VOID)
                    .addAnnotation(Override.class)
                    .addParameter(atlas);
            List<RouteMeta> groupData = entries.getValue();
            for (RouteMeta routeMeta : groupData) {
                /*
                   $S String
                   $T Class
                   $L 字面量
                 */
                method.addStatement("atlas.put($S,$T.build($T.$L,$T.class,$S,$S))",
                        routeMeta.getPath(),
                        ClassName.get(RouteMeta.class),
                        ClassName.get(RouteMeta.Type.class),
                        routeMeta.getType(),
                        ClassName.get((TypeElement) routeMeta.getElement()),
                        routeMeta.getPath(),
                        routeMeta.getGroup()
                );
            }
            //类名
            String groupName = entries.getKey();
            String groupClassName = Consts.NAME_OF_GROUP + groupName;
            //创建类
            TypeSpec typeSpec = TypeSpec.classBuilder(groupClassName)
                    .addSuperinterface(ClassName.get(iRouterGroup))
                    .addModifiers(Modifier.PUBLIC)
                    .addMethod(method.build())
                    .build();
            //文件
            JavaFile javaFile = JavaFile.builder(Consts.PACKAGE_OF_GENERATE_FILE, typeSpec).build();
            try {
                javaFile.writeTo(filerUtils);
            } catch (IOException e) {
                e.printStackTrace();
            }
            rootMap.put(groupName, groupClassName);
        }
    }

    private void generateRoot(TypeElement iRouterGroup, TypeElement iRouterRoot) {
        /*
          @Override
             public void loadInto(Map<String, Class<? extends IRouteGroup>> routes) {
                 routes.put("main", DNRouter$$Group$$main.class);
                 routes.put("a", DNRouter$$Group$$main.class);
                 routes.put("b", DNRouter$$Group$$main.class);
  }
         */

        /*
                //类型 Map<String,Class<? extends IRouteGroup>> routes>
        //Wildcard 通配符
        ParameterizedTypeName routes = ParameterizedTypeName.get(
                ClassName.get(Map.class),
                ClassName.get(String.class),
                ParameterizedTypeName.get(
                        ClassName.get(Class.class),
                        WildcardTypeName.subtypeOf(ClassName.get(iRouteGroup))
                )
        );

         */
        ParameterizedTypeName parameterizedType = ParameterizedTypeName.get(
                ClassName.get(Map.class),
                ClassName.get(String.class),
                ParameterizedTypeName.get(
                        ClassName.get(Class.class),
                        WildcardTypeName.subtypeOf(ClassName.get(iRouterGroup
                                ))
                )
        );
        ParameterSpec routes = ParameterSpec.builder(parameterizedType, "routes").build();
        MethodSpec.Builder menthod = MethodSpec.methodBuilder("loadInto")
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeName.VOID)
                .addAnnotation(Override.class)
                .addParameter(routes);
        for (Map.Entry<String, String> entries : rootMap.entrySet()) {
            //   method.addStatement("atlas.put($S,$T.build($T.$L,$T.class,$S,$S))",
            menthod.addStatement("routes.put($S,$T.class)", entries.getKey(),
                    ClassName.get(Consts.PACKAGE_OF_GENERATE_FILE, entries.getValue()));
        }
        String rootClassName = Consts.NAME_OF_ROOT + moduleName;
        TypeSpec typeSpec = TypeSpec.classBuilder(rootClassName)
                .addSuperinterface(ClassName.get(iRouterRoot))
                .addModifiers(Modifier.PUBLIC)
                .addMethod(menthod.build())
                .build();
        try {
            JavaFile.builder(Consts.PACKAGE_OF_GENERATE_FILE, typeSpec)
                    .build().writeTo(filerUtils);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    /**
     * 是否存在组  否则截取path
     *
     * @param routeMeta
     */
    private void categories(RouteMeta routeMeta) {
        if (routerVerify(routeMeta)) {
            log.i("Group : " + routeMeta.getGroup() + " path = " + routeMeta.getPath());
            List<RouteMeta> routeMetas = groupMap.get(routeMeta.getGroup());
            if (Utils.isEmpty(routeMetas)) {
                routeMetas = new ArrayList<>();
                routeMetas.add(routeMeta);
                groupMap.put(routeMeta.getGroup(), routeMetas);
            } else {
                routeMetas.add(routeMeta);
            }
        } else {
            log.i("Group Info Error" + routeMeta.getPath());
        }
    }

    private boolean routerVerify(RouteMeta routeMeta) {
        String path = routeMeta.getPath();
        String group = routeMeta.getGroup();
        if (Utils.isEmpty(path) || !path.startsWith("/")) {
            return false;
        }
        if (Utils.isEmpty(group)) {
            String defaultGroup = path.substring(1, path.indexOf("/", 1));
            if (Utils.isEmpty(defaultGroup)) {
                return false;
            }
            routeMeta.setGroup(defaultGroup);
        }
        return true;
    }
}


