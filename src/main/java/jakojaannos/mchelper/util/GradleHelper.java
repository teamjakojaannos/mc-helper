package jakojaannos.mchelper.util;

import com.intellij.openapi.externalSystem.service.execution.ExternalSystemJdkUtil;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import lombok.NonNull;
import lombok.val;
import org.gradle.tooling.BuildLauncher;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProgressListener;

import java.io.File;

public class GradleHelper {
    public static void runGradleTask(@NonNull Project project,
                                     @NonNull ProgressIndicator indicator,
                                     @NonNull Task task) {
        val connector = GradleConnector.newConnector();
        val file = new File(project.getBaseDir().getPath()); // FIXME: Might not work for submodule tasks
        connector.forProjectDirectory(file);
        val connection = connector.connect();
        val launcher = connection.newBuild();

        try {
            val sdk = ExternalSystemJdkUtil.getAvailableJdk(project);
            if (sdk.second != null
                    && sdk.second.getHomePath() != null
                    && !ExternalSystemJdkUtil.USE_INTERNAL_JAVA.equals(sdk.first)) {
                launcher.setJavaHome(new File(sdk.second.getHomePath()));
            }

            launcher.addProgressListener((ProgressListener) progressEvent ->
                    indicator.setText(progressEvent.getDescription()));

            task.run(launcher);
            launcher.run();
        } finally {
            connection.close();
        }
    }

    public interface Task {
        void run(BuildLauncher launcher);
    }
}
