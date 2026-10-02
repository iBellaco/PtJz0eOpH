import org.jetbrains.kotlin.cli.jvm.compiler.*
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.com.intellij.openapi.util.Disposer
import org.jetbrains.kotlin.com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.psi.*
import java.io.File
import org.json.JSONObject
import org.json.JSONArray
import com.example.util.TranslationCatalog
import com.example.util.TranslationAssets
import com.example.SpanishUiResidue

/** Audits real text sinks, including literals inside conditionals and interpolation. */
fun main(args: Array<String>) {
    val root = File(args[0])
    fun phrases(name: String): Map<String, String> {
        val json = JSONObject(File(root, "app/src/main/assets/$name").readText())
        return json.keys().asSequence().associateWith { json.getString(it) }
    }
    val catalog = TranslationAssets.load(::phrases)
    val disposable = Disposer.newDisposable()
    val environment = KotlinCoreEnvironment.createForProduction(disposable, CompilerConfiguration(), EnvironmentConfigFiles.JVM_CONFIG_FILES)
    val factory = KtPsiFactory(environment.project, false)
    val records = JSONArray()
    val expressions = JSONArray()
    val localization = setOf("tr", "appTr", "trStr", "trNullable", "localizedString")
    val textSinks = setOf("Text", "BasicText", "setContentTitle", "setContentText", "setSubText", "setMessage", "setTitle", "setDescription", "setHint", "setText", "setTooltipText")
    val translatedComponents = setOf("AuthTextField", "AuthHeader", "MetricRow", "DurationButton", "KpiCard", "AppAssetImage")
    root.resolve("app/src/main/java").walkTopDown().filter { it.extension == "kt" }.forEach { file ->
        val source = file.readText()
        val psi = factory.createFile(file.name, source)
        PsiTreeUtil.collectElementsOfType(psi, KtCallExpression::class.java).forEach { call ->
            val sink = call.calleeExpression?.text
            val arguments = mutableSetOf<KtValueArgument>()
            if (sink in textSinks) {
                (call.valueArguments.firstOrNull { it.getArgumentName()?.asName?.asString() == "text" }
                    ?: call.valueArguments.firstOrNull())?.let { arguments.add(it) }
            }
            if (sink == "makeText") call.valueArguments.getOrNull(1)?.let { arguments.add(it) }
            if (sink == "NotificationChannel") call.valueArguments.getOrNull(1)?.let { arguments.add(it) }
            call.valueArguments.filter { it.getArgumentName()?.asName?.asString() in setOf("contentDescription", "fallbackText", "label", "title", "subtitle", "subtext", "value") &&
                it.getArgumentExpression() !is KtLambdaExpression && sink in translatedComponents + setOf("Image", "Icon", "AsyncImage") }
                .forEach { arguments.add(it) }
            arguments.forEach { argument ->
                val value = argument.getArgumentExpression()
                if (sink in textSinks + setOf("makeText") && value != null &&
                    value !is KtStringTemplateExpression && value !is KtLambdaExpression &&
                    PsiTreeUtil.collectElementsOfType(value, KtCallExpression::class.java).none { it.calleeExpression?.text in localization }) {
                    expressions.put(JSONObject().put("path", file.relativeTo(root).path)
                        .put("line", source.take(value.textOffset).count { it == '\n' } + 1).put("expression", value.text))
                }
                PsiTreeUtil.collectElementsOfType(argument, KtStringTemplateExpression::class.java).forEach strings@ { expression ->
                    // Interpolation can contain nested literals; each one is checked at its actual sink.
                    var slot = 0
                    val text = expression.entries.joinToString("") { entry -> when(entry) {
                        is KtEscapeStringTemplateEntry -> entry.unescapedValue
                        is KtStringTemplateEntryWithExpression -> "{${slot++}}"
                        else -> entry.text
                    } }
                    if (!text.any { it.isLetter() } || text.length < 3) return@strings
                    var node: org.jetbrains.kotlin.com.intellij.psi.PsiElement? = expression.parent
                    // These components translate their string parameters before drawing them.
                    var translatedAtSink = sink in translatedComponents
                    while (node != null && node != call) {
                        if (node is KtCallExpression && node.calleeExpression?.text in localization) translatedAtSink = true
                        node = node.parent
                    }
                    val sample = text.replace(Regex("\\{\\d+\\}"), "VALUE")
                    val translated = catalog.translate("pt", sample)
                    records.put(JSONObject().put("path", file.relativeTo(root).path)
                        .put("line", source.take(expression.textOffset).count { it == '\n' } + 1)
                        .put("text", text).put("portuguese", translated).put("localizedAtSink", translatedAtSink))
                }
            }
        }
    }
    File(args[1]).writeText(records.toString(2) + "\n")
    File(args[1] + ".expressions.json").writeText(expressions.toString(2) + "\n")
    println("Visible literal occurrences=${records.length()}; output=${args[1]}")
    Disposer.dispose(disposable)
    if ("--check" in args) {
        val bypasses = (0 until records.length()).map { records.getJSONObject(it) }.filter {
            !it.getBoolean("localizedAtSink") &&
                it.getString("text").replace(Regex("\\{\\d+\\}"), "VALUE") != it.getString("portuguese")
        }
        check(bypasses.isEmpty()) { "Unlocalized text sinks: ${bypasses.joinToString()}" }
        val residues = (0 until records.length()).map { records.getJSONObject(it) }.filter {
            SpanishUiResidue.pattern.containsMatchIn(it.getString("portuguese").replace("Lee Sin", "LeeSin"))
        }
        check(residues.isEmpty()) { "Spanish in Portuguese text sinks: ${residues.joinToString()}" }
        println("PORTUGUESE_SOURCE_AUDIT: ${records.length()} literal occurrences; zero Spanish findings")
    }
}
