
import com.github.javaparser.ast.Node
import java.io.File
import java.util.*



class SpecificNodeIterator<T>(private val type: Class<T>) {
   private fun handle(node: T, descendants: LinkedList<T>) {
        descendants.add(node)
    }

    fun explore(node: Node, descendants:LinkedList<T>) {
        if (type.isInstance(node)) {
            handle(type.cast(node), descendants)
        }
        for (child in node.childNodes) {
            explore(child, descendants)
        }
    }
}

// extension function, extends the class node
fun <T> Node.descendantsOfType(type: Class<T>) : List<T> {
    val descendants = LinkedList<T>()
    SpecificNodeIterator(type).explore(this, descendants)
    return descendants
}

