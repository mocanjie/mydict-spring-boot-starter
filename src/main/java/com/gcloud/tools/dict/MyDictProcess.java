package com.gcloud.tools.dict;

import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.*;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import java.util.Set;

@SupportedAnnotationTypes({"com.gcloud.tools.dict.MyDict"})
public class MyDictProcess extends AbstractProcessor {

    private Messager messager;
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
            //生成变量
            JCTree.JCVariableDecl dictVariableDecl = makeDictDescFieldDecl(jcVariableDecl);
            jcClassDecl.defs = jcClassDecl.defs.append(dictVariableDecl);
            //生成get方法
            try {
                MyDict annotation = variableElement.getAnnotation(MyDict.class);
                JCTree.JCMethodDecl dictMethodDecl = makeGetterMethodDecl(jcVariableDecl, annotation);
                jcClassDecl.defs = jcClassDecl.defs.append(dictMethodDecl);
            }catch (Exception e){}
        }
        return true;
    }

    /**
     * 变量是否已存在
     * @param jcClassDecl
     * @param jcVariableDecl
     * @return false不存在，true存在
     */
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

    private JCTree.JCVariableDecl makeDictDescFieldDecl(JCTree.JCVariableDecl jcVariableDecl) {
        treeMaker.pos = jcVariableDecl.pos;
        return treeMaker.VarDef(treeMaker.Modifiers(jcVariableDecl.getModifiers().flags),getNewDictVarName(jcVariableDecl.getName()),memberAccess("java.lang.String"), null);
    }

    private JCTree.JCExpression memberAccess(String components){
        String[] componentArray = components.split("\\.");
        JCTree.JCExpression expr = treeMaker.Ident(getnameFromString(componentArray[0]));
        for (int i=1;i<componentArray.length;i++){
            expr = treeMaker.Select(expr,getnameFromString(componentArray[i]));
        }
        return expr;
    }

    private Name getNewGetMethodName(Name name) {
        name = getNewDictVarName(name);
        String s = name.toString();
        return names.fromString("get" + s.substring(0, 1).toUpperCase() + s.substring(1, name.length()));
    }

    private JCTree.JCMethodDecl makeGetterMethodDecl(JCTree.JCVariableDecl jcVariableDecl,MyDict annotation) {
        ListBuffer<JCTree.JCStatement> statements = new ListBuffer<>();
        statements.append(treeMaker.Return(
                treeMaker.Apply(
                        List.<JCTree.JCExpression>nil(),
                        treeMaker.Select(memberAccess("com.gcloud.tools.dict.MyDictHelper"),
                                            elementUtils.getName("getDesc")),
                        List.<JCTree.JCExpression>of(
                                treeMaker.Literal(annotation.name()),
                                treeMaker.Select(treeMaker.Ident(names.fromString("this")), jcVariableDecl.getName())
                        )
                )
        ));
        JCTree.JCBlock body = treeMaker.Block(0, statements.toList());
        return treeMaker.MethodDef(treeMaker.Modifiers(Flags.PUBLIC), getNewGetMethodName(jcVariableDecl.getName()), memberAccess("java.lang.String"), List.nil(), List.nil(), List.nil(), body, null);
    }

    private Name getnameFromString(String s){
        return names.fromString(s);
    }


    private Name getNewDictVarName(Name name) {
        return names.fromString(name.toString()+"Desc");
    }


    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.messager = processingEnv.getMessager();
        this.trees = JavacTrees.instance(processingEnv);
        Context context = ((JavacProcessingEnvironment) processingEnv).getContext();
        this.treeMaker = TreeMaker.instance(context);
        this.names = Names.instance(context);
    }
}
