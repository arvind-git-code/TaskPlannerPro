import android.content.Context
import android.content.Intent
import com.arvindmaurya.taskplannerpro.data.Task
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun shareTask(context: Context, task: Task) {
    val shareIntent = Intent().apply {
        action = Intent.ACTION_SEND
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, task.title)
        putExtra(Intent.EXTRA_TEXT, """
            Task: ${task.title}
            Description: ${task.description}
            Due Date: ${SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(task.endDate))}
            Priority: ${task.priority}
            Category: ${task.category}
        """.trimIndent())
    }
    context.startActivity(Intent.createChooser(shareIntent, "Share Task"))
} 