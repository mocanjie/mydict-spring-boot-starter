package io.github.mocanjie.tools.dict;

import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.*;

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
        return SourceVersion.RELEASE_8;
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

    private Name getNewMethodName(int methodType,Name name) {
        name = getNewDictVarName(name);
        String s = name.toString();
        String pref = methodType==0?"get":"set";
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
                                treeMaker.Select(treeMaker.Ident(names.fromString("this")), jcVariableDecl.getName())
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

    private Name getnameFromString(String s){
        return names.fromString(s);
    }




    private Name getNewDictVarName(Name name) {
        return names.fromString(name.toString()+"Desc");
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
