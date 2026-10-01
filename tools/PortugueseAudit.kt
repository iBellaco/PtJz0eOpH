import org.jetbrains.kotlin.cli.jvm.compiler.*
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.com.intellij.openapi.util.Disposer
import org.jetbrains.kotlin.com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.psi.*
import java.io.File
import org.json.JSONObject
import org.json.JSONArray
import com.example.util.TranslationCatalog
fun main(args: Array<String>) {
 val root=File(args[0]); fun map(path:String): Map<String,String> { val j=JSONObject(File(root,path).readText()); return j.keys().asSequence().associateWith{j.getString(it)} }
 val catalog=TranslationCatalog(map("app/src/main/assets/translations_pt.json"),portugueseAliases=map("app/src/main/assets/translations_pt_aliases.json"))
 val d=Disposer.newDisposable(); val env=KotlinCoreEnvironment.createForProduction(d,CompilerConfiguration(),EnvironmentConfigFiles.JVM_CONFIG_FILES);val factory=KtPsiFactory(env.project,false)
 val phrases=linkedMapOf<String,MutableSet<String>>()
 fun add(text:String,path:String){ if(text.any {it.isLetter()} && text.length>3) phrases.getOrPut(text){linkedSetOf()}.add(path) }
 root.resolve("app/src/main/java").walkTopDown().filter{it.extension=="kt"}.forEach{f->
  val p=f.relativeTo(root).path;val psi=factory.createFile(f.name,f.readText())
  if ("--wide" in args) {
  PsiTreeUtil.collectElementsOfType(psi,KtStringTemplateExpression::class.java).forEach{expr ->
   var slot=0
   val text=expr.entries.joinToString(""){when(it) { is KtEscapeStringTemplateEntry -> it.unescapedValue; is KtStringTemplateEntryWithExpression -> "{${slot++}}"; else -> it.text }}
   if(Regex("\\b(?:daño|curación|enemigos|campeones|puedes|pantalla|seleccionar|hechizos|velocidad|cerrar|guardar|obligatori[oa]|filtrar|línea|selecciona|consejo|necesitas|sesión|contraseña)\\b|[¿¡ñ]",RegexOption.IGNORE_CASE).containsMatchIn(text))add(text,p)
  }

  } else {
  PsiTreeUtil.collectElementsOfType(psi,KtCallExpression::class.java).filter{it.calleeExpression?.text in setOf("tr","appTr","trStr","Text")}.forEach{call->
   val arg=if(call.calleeExpression?.text=="trStr") call.valueArguments.getOrNull(1) else call.valueArguments.firstOrNull{it.getArgumentName()?.asName?.asString()=="text"}?:call.valueArguments.firstOrNull()
   val expr=arg?.getArgumentExpression() as? KtStringTemplateExpression
   if(expr!=null) {
    var slot=0; val text=expr.entries.joinToString(""){when(it) { is KtEscapeStringTemplateEntry -> it.unescapedValue; is KtStringTemplateEntryWithExpression -> "{${slot++}}"; else -> it.text }}; add(text,p)
   }
  }
  }
 }
 val uiCount=phrases.size
 val fields=setOf("title","description","stats","passive","coachTip","buildTitle","itemName","runeName","spellName")
 fun walk(value:Any,path:String) {
  when(value) {
   is JSONObject -> value.keys().asSequence().forEach{key->val child=value.get(key);if(child is String && key in fields)add(child,path) else if(child is JSONObject || child is JSONArray)walk(child,path)}
   is JSONArray -> (0 until value.length()).forEach{walk(value.get(it),path)}
  }
 }
 for(dir in listOf("app/src/main/assets","app/src/main/res/raw"))root.resolve(dir).walkTopDown().filter{it.extension=="json" && !it.name.startsWith("translations")}.forEach{f->
  val text=f.readText(); val obj=if(text.trim().startsWith("["))JSONArray(text) else JSONObject(text); walk(obj,f.relativeTo(root).path)
 }
 val unchanged=JSONArray(); phrases.forEach{(phrase,paths)->if(catalog.translate("pt",phrase.replace(Regex("\\{\\d+\\}"),"VALUE"))==phrase.replace(Regex("\\{\\d+\\}"),"VALUE"))unchanged.put(JSONObject().put("text",phrase).put("paths",JSONArray(paths.toList())))}
 File(args.getOrElse(1) { "/tmp/coach-pt-unchanged.json" }).writeText(unchanged.toString(2))
 println("Kotlin phrases=$uiCount; total phrases=${phrases.size}; unchanged=${unchanged.length()}; output=${args.getOrElse(1) { "/tmp/coach-pt-unchanged.json" }}")
 Disposer.dispose(d)
}
