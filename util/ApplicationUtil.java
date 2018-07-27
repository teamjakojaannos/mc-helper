package jakojaannos.mchelper.util;

import com.intellij.openapi.application.ApplicationManager;

public class ApplicationUtil {
    private ApplicationUtil() {
    }

    public static void runWriteAction(Runnable action) {
        if (ApplicationManager.getApplication().isWriteAccessAllowed()) {
            action.run();
        } else {
            invokeAndWait(() -> ApplicationManager.getApplication().runWriteAction(action));
        }
    }

    public static void invokeAndWait(Runnable action) {
        if (ApplicationManager.getApplication().isDispatchThread()) {
            action.run();
        } else {
            ApplicationManager.getApplication().invokeAndWait(action);
        }
    }
}
