package com.exemple.tp2;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.*;

public class Parser {
    private static final String benPathProject = "/Users/benjaminadolphe/Downloads/IntelligenceArtificielleTP2-main";
    private static final String benPathJre = "/Users/benjaminadolphe/Library/Java/JavaVirtualMachines/openjdk-16.0.1/Contents/Home/bin";
    public static final String projectPath = benPathProject;
    public static final String projectSourcePath = projectPath + "/src";
    public static final String jrePath = benPathJre;

    public static int class_compter = 0;
    public static int method_compter = 0;
    public static int fields_compter = 0;
    public static int max_parameter = 0;
    public static int app_line_compter = 0;

    public static List<String> packageList = new ArrayList<>();
    public static List<String> linePerMethodList = new ArrayList<>();
    public static List<String> classesWithMostMethods = new ArrayList<>();
    public static List<String> classesWithMostFields = new ArrayList<>();

    public static List<Map<String, Integer>> methodsWithNumberOfLinesByClass = new ArrayList<>();

    public static HashMap<String, Integer> classesMethodsHashMap = new HashMap<>();
    public static HashMap<String, Integer> classesFieldsHashMap = new HashMap<>();

    public static void main(String[] args) throws IOException {

        // read java files
        final File folder = new File(projectSourcePath);
        ArrayList<File> javaFiles = listJavaFilesForFolder(folder);

        //
        for (File fileEntry : javaFiles) {
            String content = FileUtils.readFileToString(fileEntry);
            // System.out.println(content);

            CompilationUnit parse = parse(content.toCharArray());

            // print methods info
            //printMethodInfo(parse);

            // print variables info
            // printVariableInfo(parse);

            //print method invocations
            //printMethodInvocationInfo(parse);

            countNumberClass(parse);
            countNumberPackages(parse);
            getNumberOfLinesPerMethod(parse);
            getAverageNumberOfFieldsPerClass(parse);
            putClassesMethodsInHashMap(parse);
            putClassesFieldsInHashMap(parse);
            getMethodsWithLines(parse);
            getMaxParameters(parse);
            getTotalNumberOfLines(parse);
        }

        //Nombre de classes de l'application
        System.out.println("---------------------------------------------------------------------");
        System.out.println("Nombre de classes de l'application -> " + class_compter);

        //Nombre de méthodes de l'application
        System.out.println("---------------------------------------------------------------------");
        System.out.println("Nombre de méthodes de l'application -> " + method_compter);

        //Nombre de package
        System.out.println("---------------------------------------------------------------------");
        System.out.println("Nombre de paquets de l'application -> " + packageList.stream().distinct().toList().size());

        //Nombre moyen de méthodes par classes
        System.out.println("---------------------------------------------------------------------");
        System.out.println("Nombre moyen de méthodes par classes -> " + (method_compter / class_compter));

        //Nombre de lignes de codes par méthode
        System.out.println("------Lignes de codes par méthodes ------");
        linePerMethodList.forEach(System.out::println);
        System.out.println("-----------------------------------------");

        //Nombre moyens d'attributs par classe
        System.out.println("Nombre moyen d'attributs par classes -> " + (fields_compter / class_compter));

        //Les 10% de classes qui possèdent le plus grand nombre de méthodes
        System.out.println("---------------------------------------------------------------------");
        System.out.println("Les 10% de classes avec le plus grand nombre de méthodes");
        getClassesWithMostMethods();
        classesWithMostMethods.forEach(System.out::println);

        //Les 10% des classes qui possèdent le plus grand nombre d'attributs
        System.out.println("---------------------------------------------------------------------");
        System.out.println("Les 10% de classes avec le plus grand nombre d'attributs");
        getClassesWithMostFields();
        classesWithMostFields.forEach(System.out::println);

        //Les classes appartenant aux deux précédentes
        System.out.println("---------------------------------------------------------------------");
        System.out.println("Les classes appartenant aux deux catégories différentes");
        getClassesWithMostFieldsAndMethods().forEach(System.out::println);

        //Les classes qui possèdent plus de X méthodes (la valeur de X est donnée)
        System.out.println("---------------------------------------------------------------------");
        System.out.println("Veuillez entrer un nombre afin de voir les classes qui possèdent plus que ce nombre de méthode :");
        Scanner userScan = new Scanner(System.in);
        String x = userScan.nextLine();
        System.out.println("Voici les différentes classes avec plus de " + x + " méthodes : ");
        moreThanXMethods(Integer.parseInt(x));

        //Les 10% des méthodes qui possèdent le plus grand nombre de lignes de code (par classe).
        System.out.println("---------------------------------------------------------------------");
        System.out.println("Les 10% des méthodes qui possèdent le plus grand nombre de lignes de code (par classe)");
        getMethodsWithMostLines().forEach(System.out::println);

        //Le nombre maximal de paramètres par rapport à toutes les méthodes de l’application.
        System.out.println("---------------------------------------------------------------------");
        System.out.println("Le nombre maximal de paramètres est : " + max_parameter);

        //Nombre de lignes totales du code
        System.out.println("---------------------------------------------------------------------");
        System.out.println("Nombre total de lignes de code -> " + app_line_compter);
    }

    // read all java files from specific folder
    public static ArrayList<File> listJavaFilesForFolder(final File folder) {
        ArrayList<File> javaFiles = new ArrayList<File>();
        for (File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                javaFiles.addAll(listJavaFilesForFolder(fileEntry));
            } else if (fileEntry.getName().contains(".java")) {
                // System.out.println(fileEntry.getName());
                javaFiles.add(fileEntry);
            }
        }

        return javaFiles;
    }

    // create AST
    private static CompilationUnit parse(char[] classSource) {
        ASTParser parser = ASTParser.newParser(AST.JLS4); // java +1.6
        parser.setResolveBindings(true);
        parser.setKind(ASTParser.K_COMPILATION_UNIT);

        parser.setBindingsRecovery(true);

        Map options = JavaCore.getOptions();
        parser.setCompilerOptions(options);

        parser.setUnitName("");

        String[] sources = {projectSourcePath};
        String[] classpath = {jrePath};

        parser.setEnvironment(classpath, sources, new String[]{"UTF-8"}, true);
        parser.setSource(classSource);

        return (CompilationUnit) parser.createAST(null); // create and parse
    }

    // navigate method information
    public static void printMethodInfo(CompilationUnit parse) {
        MethodDeclarationVisitor visitor = new MethodDeclarationVisitor();
        parse.accept(visitor);

        for (MethodDeclaration method : visitor.getMethods()) {
            System.out.println("Method name: " + method.getName()
                    + " Return type: " + method.getReturnType2());
        }

    }

    // navigate variables inside method
    public static void printVariableInfo(CompilationUnit parse) {

        MethodDeclarationVisitor visitor1 = new MethodDeclarationVisitor();
        parse.accept(visitor1);
        for (MethodDeclaration method : visitor1.getMethods()) {

            VariableDeclarationFragmentVisitor visitor2 = new VariableDeclarationFragmentVisitor();
            method.accept(visitor2);

            for (VariableDeclarationFragment variableDeclarationFragment : visitor2
                    .getVariables()) {
                System.out.println("variable name: "
                        + variableDeclarationFragment.getName()
                        + " variable Initializer: "
                        + variableDeclarationFragment.getInitializer());
            }

        }
    }

    // navigate method invocations inside method
    public static void printMethodInvocationInfo(CompilationUnit parse) {

        MethodDeclarationVisitor visitor1 = new MethodDeclarationVisitor();
        parse.accept(visitor1);
        for (MethodDeclaration method : visitor1.getMethods()) {

            MethodInvocationVisitor visitor2 = new MethodInvocationVisitor();
            method.accept(visitor2);

            for (MethodInvocation methodInvocation : visitor2.getMethods()) {
                System.out.println("method " + method.getName() + " invoc method "
                        + methodInvocation.getName());
            }

        }
    }

    public static void countNumberClass(CompilationUnit parse) {
        TypeDeclarationVisitor typeDeclarationVisitor = new TypeDeclarationVisitor();
        parse.accept(typeDeclarationVisitor);
        typeDeclarationVisitor.getTypes().forEach(typeDeclaration -> {
            if (!typeDeclaration.isInterface()) {
                class_compter += 1;
                method_compter += typeDeclaration.getMethods().length;
            }
        });
    }

    public static void countNumberPackages(CompilationUnit parse) {
        PackageVisitor packageVisitor = new PackageVisitor();
        parse.accept(packageVisitor);
        packageVisitor.getPackageDeclarations().forEach(packageDeclaration -> packageList.add(packageDeclaration.getName().toString()));
    }

    public static void getNumberOfLinesPerMethod(CompilationUnit parse) {
        MethodDeclarationVisitor visitor = new MethodDeclarationVisitor();
        parse.accept(visitor);
        for (MethodDeclaration method : visitor.getMethods()) {
            linePerMethodList.add("La méthode " + method.getName() + " a " + getNumberOfLineOfAMethod(parse, method) + " lignes de codes");
        }
    }

    public static int getNumberOfLineOfAMethod(CompilationUnit parse, MethodDeclaration method) {
        if (method.getBody() == null) {
            return 0;
        }

        int beginning = parse.getLineNumber(method.getBody().getStartPosition());
        int end = parse.getLineNumber(method.getBody().getStartPosition() + method.getBody().getLength());

        return Math.max(end - beginning - 1, 0);
    }

    public static void getAverageNumberOfFieldsPerClass(CompilationUnit parse) {
        TypeDeclarationVisitor typeDeclarationVisitor = new TypeDeclarationVisitor();
        parse.accept(typeDeclarationVisitor);
        typeDeclarationVisitor.getTypes().forEach(typeDeclaration -> {
            if (!typeDeclaration.isInterface()) {
                fields_compter += typeDeclaration.getFields().length;
            }
        });
    }

    public static void putClassesMethodsInHashMap(CompilationUnit parse) {
        TypeDeclarationVisitor visitor = new TypeDeclarationVisitor();
        parse.accept(visitor);
        visitor.getTypes().forEach(type -> {
            if (!type.isInterface())
                classesMethodsHashMap.put(type.getName().toString(), type.getMethods().length);
        });
    }

    public static void getClassesWithMostMethods() {
        int numberOfClasses = (int) (0.1 * classesMethodsHashMap.size());

        List<String> classes = classesMethodsHashMap.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        classesWithMostMethods = classes.subList(0, numberOfClasses);
    }

    public static void putClassesFieldsInHashMap(CompilationUnit parse) {
        TypeDeclarationVisitor visitor = new TypeDeclarationVisitor();
        parse.accept(visitor);

        visitor.getTypes().forEach(type -> {
            if (!type.isInterface()) {
                classesFieldsHashMap.put(type.getName().toString(), type.getFields().length);
            }
        });
    }

    public static void getClassesWithMostFields() {
        int numberOfClasses = (int) (0.1 * classesFieldsHashMap.size());

        List<String> classes = classesFieldsHashMap.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        classesWithMostFields = classes.subList(0, numberOfClasses);
    }

    public static List<String> getClassesWithMostFieldsAndMethods() {
        List<String> res = new ArrayList<String>(classesWithMostMethods);
        res.retainAll(classesWithMostFields);
        return res;
    }

    public static void moreThanXMethods(int x) {
        classesMethodsHashMap.forEach((key, value) -> {
            if (value > x) {
                System.out.println(key);
            }
        });
    }

    public static void getMethodsWithLines(CompilationUnit parse) {
        TypeDeclarationVisitor visitor = new TypeDeclarationVisitor();
        parse.accept(visitor);

        for (TypeDeclaration type : visitor.getTypes()) {
            if (type.isInterface())
                continue;

            Map<String, Integer> methodsWithLines = new HashMap<String, Integer>();

            for (MethodDeclaration method : type.getMethods())
                methodsWithLines.put(type.getName() + "." + method.getName(), getNumberOfLineOfAMethod(parse, method));

            methodsWithNumberOfLinesByClass.add(methodsWithLines);
        }
    }

    public static List<String> getMethodsWithMostLines() {

        List<String> methodsWithMostLines = new ArrayList<String>();

        for (Map<String, Integer> methodsWithLines : methodsWithNumberOfLinesByClass) {
            int numberOfMethods = (int) Math.ceil(0.1 * methodsWithLines.size());

            List<String> methods = methodsWithLines.entrySet()
                    .stream()
                    .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());

            methodsWithMostLines.addAll(methods.subList(0, numberOfMethods));
        }

        return methodsWithMostLines;
    }

    public static void getMaxParameters(CompilationUnit parse) {
        MethodDeclarationVisitor visitor = new MethodDeclarationVisitor();
        parse.accept(visitor);
        visitor.getMethods().forEach(methodDeclaration -> {
            if (methodDeclaration.parameters().size() > max_parameter) {
                max_parameter = methodDeclaration.parameters().size();
            }
        });
    }

    public static void getTotalNumberOfLines(CompilationUnit parse) {
        TypeDeclarationVisitor visitor = new TypeDeclarationVisitor();
        parse.accept(visitor);

        visitor.getTypes().forEach(typeDeclaration -> {
            int beginning = parse.getLineNumber(typeDeclaration.getStartPosition());
            int end = parse.getLineNumber(typeDeclaration.getStartPosition() + typeDeclaration.getLength());
            app_line_compter += Math.max((end - beginning), 0);
        });
        PackageVisitor visitor1 = new PackageVisitor();
        parse.accept(visitor1);
        app_line_compter += visitor1.getPackageDeclarations().size();
    }
}
