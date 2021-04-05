
import com.github.javaparser.JavaParser
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.expr.MethodCallExpr
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver
import java.io.File
import java.util.*

import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver
import com.github.javaparser.symbolsolver.resolution.typesolvers.JarTypeSolver


fun findJavaFiles(dir: File) : List<File> {
    var files = LinkedList<File>()
    DirExplorer().goToFilePath(dir, files)
    return files
}

fun typeSolver() : TypeSolver {
    val combinedTypeSolver = CombinedTypeSolver()
    combinedTypeSolver.add(JavaParserTypeSolver(File("src/main/resources/javaparser")))
    combinedTypeSolver.add(JavaParserTypeSolver(File("src/main/resources/javaparser-core")))
    combinedTypeSolver.add(JavaParserTypeSolver(File("src/main/resources/javaparser-generated-sources")))
    combinedTypeSolver.add(JarTypeSolver(File("give jre address here")))
    return combinedTypeSolver
}

class DirExplorer {

    fun goToFilePath(root: File, filesList:LinkedList<File>) {
        goToFilePath(0, "", root, filesList)
    }

    private fun isJavaFile(file: File): Boolean{
        return file.path.endsWith(".java")
    }

    private fun goToFilePath(level: Int, path: String, file: File, filesList:LinkedList<File>){
        if (file.isDirectory) {
            for (child in file.listFiles()) {
                goToFilePath(level + 1, path + "/" + child.name, child, filesList)
            }
        } else {
            if (isJavaFile(file)) {
                filesList.add(file)
            }
        }
    }
}

var solved = 0
var unsolved = 0
var errors = 0


fun processJavaFile(file: File, javaParserFacade: JavaParserFacade) {
    println(file)
    val parser = JavaParser()
    val compilationUnitParse = parser.parse(file)
    var compilationUnit: CompilationUnit? = null
    if (compilationUnitParse.isSuccessful && compilationUnitParse.result.isPresent) {
        compilationUnit = compilationUnitParse.result.get()
    }

    compilationUnit?.descendantsOfType(MethodCallExpr::class.java)?.forEach {
        print(" * ${it.begin} $it ")
        try {
            val methodRef = javaParserFacade.solveMethodAsUsage(it)
            if (methodRef != null) {
                solved++
                val methodDecl = methodRef
                println("  -> I got solved")
            } else {
                unsolved++

            }
        } catch (e: Exception) {
            println(" ERR ${e.message}")
            errors++
        } catch (t: Throwable) {
//            t.printStackTrace()
        }
    }
}
//    compilationUnit?.accept(object : VoidVisitorAdapter<Any?>() {
////        override fun visit(n: NameExpr, arg: Any?) {
////            val type = javaParserFacade.getType(n)
////            super.visit(n, arg)
////            println("  -> ${type}")
////        }
////
//        override fun visit(n: MethodCallExpr, arg: Any?) {
//            val decl = javaParserFacade.solve(n)?.correspondingDeclaration
//            super.visit(n, arg)
//            println("  -> ${decl}")
//        }
//    }, null)
//    compilationUnit?.descendantsOfType(MethodCallExpr::class.java)?.forEach {
//        print(" * L${it.begin} $it ")
//        try {
//            val methodRef = javaParserFacade.solveMethodAsUsage(it)
//            if (methodRef != null) {
//                solved++
//                val methodDecl = methodRef
//                println("  -> I got solved")
//            } else {
//                unsolved++
//
//            }
//        } catch (e: Exception) {
//            println(" ERR ${e.message}")
//            errors++
//        } catch (t: Throwable) {
//            t.printStackTrace()
//        }
//    }


fun main(args:Array<String>) {
    val javaFiles = findJavaFiles(File("src/main/java/"))
    val myTypeSolver = JavaParserFacade.get(typeSolver())
    javaFiles.forEach { processJavaFile(it, myTypeSolver) }
    println("solved $solved unsolved $unsolved errors $errors")
}