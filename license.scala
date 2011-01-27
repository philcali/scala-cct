import scala.io.Source.{fromFile => open}
import java.io.File
import com.philipcali.cct.Utils.recurse
import java.io.FileWriter

// Run this script with the cct.jar in your classpath
object Lisence {
  val text = open("LICENSE.md").getLines.map("// " + _).mkString("\n")

  def apply(input: File) {
    val source = open(input).getLines.mkString("\n")

    // If the source contains copyright, ignore it.
    if(!source.contains(text)) {
      val writer = new FileWriter(input)
      writer.write(text + "\n" + source)
      writer.close()
    }
  }
}

// Apply Copyright to any file that doesn't have it
recurse(new File(".")) { f => 
  if(f.getName.endsWith(".scala")) Lisence(f) 
}
