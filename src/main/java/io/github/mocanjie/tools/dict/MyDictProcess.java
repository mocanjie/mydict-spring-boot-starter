package io.github.mocanjie.tools.dict;

import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.*;
import io.github.mocanjie.tools.dict.entity.Var;
import io.github.mocanjie.tools.dict.entity.VarType;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import java.util.Set;

@SupportedAnnotationTypes({"io.github.mocanjie.tools.dict.MyDict"})
public class MyDictProcess extends AbstractProcessor {

    private JavacTrees trees;
    private TreeMaker treeMaker;
    private Names names;
    private JavacElements elementUtils;

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        elementUtils = (JavacElements) processingEnv.getElementUtils();
        Set<? extends VariableElement> set = (Set<? extends VariableElement>) roundEnv.getElementsAnnotatedWith(MyDict.class);

        for (VariableElement variableElement : set) {
            JCTree.JCVariableDecl jcVariableDecl = (JCTree.JCVariableDecl) trees.getTree(variableElement);
            JCTree.JCClassDecl jcClassDecl = (JCTree.JCClassDecl) trees.getTree(variableElement.getEnclosingElement());
            if(isVariableExist(jcClassDecl,jcVariableDecl)) continue;
            MyDict annotation = variableElement.getAnnotation(MyDict.class);
            //生成变量
            JCTree.JCVariableDecl dictVariableDecl = makeDictDescFieldDecl(jcVariableDecl,annotation);
            jcClassDecl.defs = jcClassDecl.defs.append(dictVariableDecl);
            //生成get方法
            try {
                JCTree.JCMethodDecl dictMethodDecl = makeGetterMethodDecl(jcVariableDecl, annotation);
                jcClassDecl.defs = jcClassDecl.defs.append(dictMethodDecl);
            }catch (Exception e){}
            try{
                jcClassDecl.defs = jcClassDecl.defs.append(makeSetterMethod(jcVariableDecl));
            }catch (Exception e){}
        }
        return true;
    }

    private boolean isVariableExist(JCTree.JCClassDecl jcClassDecl, JCTree.JCVariableDecl jcVariableDecl){
        Name dictVarName = getNewDictVarName(jcVariableDecl.getName());
        return jcClassDecl.defs.stream().filter(x -> {
            if (x.getTree() instanceof JCTree.JCVariableDecl) {
                JCTree.JCVariableDecl tree = (JCTree.JCVariableDecl) x.getTree();
                return tree.getName().toString().equals(dictVarName.toString());
            }else {
                return false;
            }
        }).findAny().isPresent();
    }

    private JCTree.JCVariableDecl makeDictDescFieldDecl(JCTree.JCVariableDecl jcVariableDecl,MyDict dict) {
        treeMaker.pos = jcVariableDecl.pos;
        ListBuffer<JCTree.JCAnnotation> annotationsList = new ListBuffer<>();
        try {
            // 检查MyBatis-Plus是否存在于classpath
            Class.forName("com.baomidou.mybatisplus.annotation.TableField");
            JCTree.JCExpression attr1 = treeMaker.Assign(treeMaker.Ident(names.fromString("exist")),
                    treeMaker.Literal(false));
            JCTree.JCAnnotation jcAnnotation = treeMaker.Annotation(memberAccess("com.baomidou.mybatisplus.annotation.TableField"),
                    List.of(attr1));
            annotationsList.append(jcAnnotation);
        }catch (Throwable e){
            // MyBatis-Plus不存在，跳过添加@TableField注解
        }
        if(dict.fieldAnnotations()!=null && dict.fieldAnnotations().length>0){
            FieldAnnotation[] annotations = dict.fieldAnnotations();
            for (FieldAnnotation annotation : annotations) {
                String fullAnnotationName = annotation.fullAnnotationName();
                Var[] vars = annotation.vars();
                ListBuffer<JCTree.JCExpression> varsList = new ListBuffer<>();
                for (Var var : vars) {
                    Object typeTagValue = getTypeTagValue(var.varType(), var.varValue());
                    JCTree.JCExpression attr1 = treeMaker.Assign(treeMaker.Ident(names.fromString(var.varName())),
                            treeMaker.Literal(typeTagValue));
                    varsList.append(attr1);
                }
                JCTree.JCAnnotation jcAnnotation = treeMaker.Annotation(memberAccess(fullAnnotationName), varsList.toList());
                annotationsList.append(jcAnnotation);
            }
        }
        return treeMaker.VarDef(treeMaker.Modifiers(jcVariableDecl.getModifiers().flags,annotationsList.toList()),getNewDictVarName(jcVariableDecl.getName()),memberAccess("java.lang.String"), null);
    }

    private Object getTypeTagValue(VarType varType,String varValue){
        if(varType.name().equalsIgnoreCase(TypeTag.SHORT.toString())) return Integer.parseInt(varValue);
        if(varType.name().equalsIgnoreCase(TypeTag.LONG.toString())) return Long.parseLong(varValue);
        if(varType.name().equalsIgnoreCase(TypeTag.FLOAT.toString())) return Float.parseFloat(varValue);
        if(varType.name().equalsIgnoreCase(TypeTag.INT.toString())) return Integer.parseInt(varValue);
        if(varType.name().equalsIgnoreCase(TypeTag.DOUBLE.toString())) return Double.parseDouble(varValue);
        if(varType.name().equalsIgnoreCase(TypeTag.BOOLEAN.toString())) return Boolean.parseBoolean(varValue);
        return varValue;
    }

    private JCTree.JCExpression memberAccess(String components){
        String[] componentArray = components.split("\\.");
        JCTree.JCExpression expr = treeMaker.Ident(getNameFromString(componentArray[0]));
        for (int i=1;i<componentArray.length;i++){
            expr = treeMaker.Select(expr,getNameFromString(componentArray[i]));
        }
        return expr;
    }

    /**
     * 生成 getter/setter 方法名
     * 自动处理驼峰命名和蛇形命名
     *
     * 示例：
     * - sexTypeDesc -> getSexTypeDesc / setSexTypeDesc
     * - sex_type_desc -> getSex_type_desc / setSex_type_desc
     */
    private Name getNewMethodName(int methodType,Name name) {
        name = getNewDictVarName(name);
        String s = name.toString();
        String pref = methodType==0?"get":"set";

        // 如果是蛇形命名，保持原样（只在前面加 get/set）
        if (s.contains("_")) {
            return names.fromString(pref + s.substring(0, 1).toUpperCase() + s.substring(1));
        }

        // 驼峰命名：首字母大写
        return names.fromString(pref + s.substring(0, 1).toUpperCase() + s.substring(1, name.length()));
    }

    private JCTree.JCMethodDecl makeGetterMethodDecl(JCTree.JCVariableDecl jcVariableDecl,MyDict annotation) {
        ListBuffer<JCTree.JCStatement> statements = new ListBuffer<>();

        JCTree.JCVariableDecl descStr = treeMaker.VarDef(
                treeMaker.Modifiers(0), names.fromString("descStr"), memberAccess("java.lang.String"),treeMaker.Apply(
                        List.<JCTree.JCExpression>nil(),
                        treeMaker.Select(memberAccess("io.github.mocanjie.tools.dict.MyDictHelper"),
                                elementUtils.getName("getDesc")),
                        List.<JCTree.JCExpression>of(
                                treeMaker.Literal(annotation.name()),
                                treeMaker.Apply(List.<JCTree.JCExpression>nil()
                                        ,treeMaker.Select(memberAccess("this")
                                                ,elementUtils.getName(getterMethodName(jcVariableDecl)))
                                            ,List.<JCTree.JCExpression>nil()
                                        )
//                                treeMaker.Select(memberAccess("this"),elementUtils.getName(getterMethodName(jcVariableDecl)))
//                                treeMaker.Select(treeMaker.Ident(names.fromString("this")), jcVariableDecl.getName())
                        )
                ));
        statements.append(descStr);

        JCTree.JCMethodInvocation apply = treeMaker.Apply(
                com.sun.tools.javac.util.List.nil(),
                treeMaker.Select(memberAccess("org.springframework.util.StringUtils"),
                        elementUtils.getName("hasText")),
                com.sun.tools.javac.util.List.of(treeMaker.Ident(names.fromString("descStr")))
        );

        JCTree.JCStatement ifTrue = treeMaker.Return(treeMaker.Ident(names.fromString("descStr")));
        JCTree.JCStatement ifFlase = treeMaker.Return(treeMaker.Literal(annotation.defaultDesc()));

        JCTree.JCIf anIf = treeMaker.If(
                apply,
                ifTrue,
                ifFlase
        );
        statements.append(anIf);
        JCTree.JCBlock body = treeMaker.Block(0, statements.toList());
        return treeMaker.MethodDef(treeMaker.Modifiers(Flags.PUBLIC), getNewMethodName(0,jcVariableDecl.getName()), memberAccess("java.lang.String"), List.nil(), List.nil(), List.nil(), body, null);
    }

    private Name getNameFromString(String s){
        return names.fromString(s);
    }

    private Name getVarName(Name name) {
        return names.fromString(name.toString());
    }


    /**
     * 根据原字段名的命名风格，生成对应的描述字段名
     * 支持驼峰命名和蛇形命名的自动识别
     *
     * 示例：
     * - sexType -> sexTypeDesc (驼峰命名)
     * - sex_type -> sex_type_desc (蛇形命名)
     * - SEX_TYPE -> SEX_TYPE_DESC (大写蛇形)
     */
    private Name getNewDictVarName(Name name) {
        String originalName = name.toString();

        // 判断是否包含下划线（蛇形命名）
        if (originalName.contains("_")) {
            // 蛇形命名：在末尾添加 _desc 或 _DESC
            // 判断原名是否全大写
            if (originalName.equals(originalName.toUpperCase())) {
                // 全大写蛇形：SEX_TYPE -> SEX_TYPE_DESC
                return names.fromString(originalName + "_DESC");
            } else {
                // 小写或混合蛇形：sex_type -> sex_type_desc
                return names.fromString(originalName + "_desc");
            }
        } else {
            // 驼峰命名：sexType -> sexTypeDesc
            return names.fromString(originalName + "Desc");
        }
    }

    private Name getterMethodName(JCTree.JCVariableDecl jcVariableDecl) {
        String s = jcVariableDecl.getName().toString();
        return names.fromString("get" + s.substring(0, 1).toUpperCase() + s.substring(1, jcVariableDecl.getName().length()));
    }

    private Name setterMethodName(JCTree.JCVariableDecl jcVariableDecl) {
        String s = jcVariableDecl.getName().toString();
        return names.fromString("set" + s.substring(0, 1).toUpperCase() + s.substring(1, jcVariableDecl.getName().length()));
    }

    private JCTree.JCMethodDecl makeSetterMethod(JCTree.JCVariableDecl jcVariableDecl){
        JCTree.JCModifiers jcModifiers = treeMaker.Modifiers(Flags.PUBLIC);
        JCTree.JCExpression retrunType = treeMaker.TypeIdent(TypeTag.VOID);
        List<JCTree.JCVariableDecl> parameters = List.nil();
        JCTree.JCVariableDecl param = treeMaker.VarDef(
                treeMaker.Modifiers(Flags.PARAMETER), getNewDictVarName(jcVariableDecl.name), memberAccess("java.lang.String"), null);
        param.pos = jcVariableDecl.pos;
        parameters = parameters.append(param);
        JCTree.JCStatement jcStatement = treeMaker.Exec(treeMaker.Assign(
                treeMaker.Select(treeMaker.Ident(names.fromString("this")), getNewDictVarName(jcVariableDecl.name)),
                treeMaker.Ident(getNewDictVarName(jcVariableDecl.name))));
        List<JCTree.JCStatement> jcStatementList = List.nil();
        jcStatementList = jcStatementList.append(jcStatement);
        JCTree.JCBlock jcBlock = treeMaker.Block(0, jcStatementList);
        List<JCTree.JCTypeParameter> methodGenericParams = List.nil();
        List<JCTree.JCExpression> throwsClauses = List.nil();
        JCTree.JCMethodDecl jcMethodDecl = treeMaker.MethodDef(jcModifiers, getNewMethodName(1,jcVariableDecl.getName()), retrunType, methodGenericParams, parameters, throwsClauses, jcBlock,  null);
        return jcMethodDecl;
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.trees = JavacTrees.instance(processingEnv);
        Context context = ((JavacProcessingEnvironment) processingEnv).getContext();
        this.treeMaker = TreeMaker.instance(context);
        this.names = Names.instance(context);
    }
}
