package com.example.compiler.processor;

import com.example.compiler.utils.Consts;
import com.example.compiler.utils.Log;
import com.example.compiler.utils.Utils;
import com.example.router.annotation.Route;
import com.example.router.annotation.model.RouteMeta;
import com.google.auto.service.AutoService;

import java.lang.annotation.Annotation;
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
    private Map<String, String> rootMao = new TreeMap<>();
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
        for (Element element : routerElements){
            //类信息
            TypeMirror typeMirror = element.asType();
            log.i("Route class" + typeMirror.toString());
            Route route = element.getAnnotation(Route.class);
            //只能在指定的类上面使用
            if (typeUtils.isSubtype(typeMirror,typeElement.asType())){
              new RouteMeta(RouteMeta.Type.ACTIVITY,route,element);
            }else {
                throw  new RuntimeException("Just support activity Router"+element);
            }

        }
    }
}


