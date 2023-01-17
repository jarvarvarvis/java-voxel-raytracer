package me.jarvis;

import com.google.common.eventbus.Subscribe;
import me.jarvis.event.EventManager;
import me.jarvis.event.events.CursorPosEvent;
import me.jarvis.event.events.KeyEvent;
import me.jarvis.util.ScreenshotUtil;
import me.jarvis.util.vector.Vector3;
import me.jarvis.raytracing.Camera;
import me.jarvis.raytracing.RaytraceContext;
import me.jarvis.raytracing.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GLUtil;
import org.lwjgl.system.Callback;

import java.io.IOException;
import java.time.Instant;

public class PixelEngine {

    public static Logger log = LogManager.getLogger(PixelEngine.class);

    private final PixelEngineWindow window;

    private final Camera camera;
    private final RaytraceContext raytraceContext;

    public static void init() {
        GLFWErrorCallback.createPrint(System.err).set();

        if (!GLFW.glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");
    }

    public PixelEngine(int width, int height) throws IOException {
        this.window = new PixelEngineWindow(width, height, "PixelEngine");

        GLFW.glfwSetInputMode(this.window.getHandle(), GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_DISABLED);

        World world = new World(32, 32, 32);
        this.camera = new Camera(new Vector3(0, 0, -20), 8, 330, 0.2f);
        this.raytraceContext = new RaytraceContext(world, camera);

        EventManager.register(this);
    }

    private float lastX = 0, lastY = 0;

    @Subscribe
    private void handleCursorPos(CursorPosEvent event) {
        float diffX = (float) (lastX - event.getX());
        camera.updateYaw(diffX);
        float diffY = (float) (lastY - event.getY());
        camera.updatePitch(diffY);

        lastX = (float) event.getX();
        lastY = (float) event.getY();
    }

    private void createAndSaveScreenshot() {
        int width = this.window.getWidth();
        int height = this.window.getHeight();
        String name = String.format("screenshot-%s.png", Instant.now());
        try {
            ScreenshotUtil.takeScreenshot(width, height, name);
        } catch (IOException ioe) {
            log.error("Failed to save screenshot as {}!", name);
        }
    }

    @Subscribe
    private void handleKey(KeyEvent event) {
        int key = event.getKey();
        int action = event.getAction();

        boolean press = action == GLFW.GLFW_PRESS;
        boolean pressOrRepeat = press || action == GLFW.GLFW_REPEAT;

        if (pressOrRepeat && key == GLFW.GLFW_KEY_W) {
            camera.moveForwardBackward(1.0f);
        }
        if (pressOrRepeat && key == GLFW.GLFW_KEY_S) {
            camera.moveForwardBackward(-1.0f);
        }
        if (pressOrRepeat && key == GLFW.GLFW_KEY_A) {
            camera.moveLeftRight(1.0f);
        }
        if (pressOrRepeat && key == GLFW.GLFW_KEY_D) {
            camera.moveLeftRight(-1.0f);
        }
        if (pressOrRepeat && key == GLFW.GLFW_KEY_SPACE) {
            camera.moveUp(1.0f);
        }
        if (pressOrRepeat && key == GLFW.GLFW_KEY_C) {
            camera.moveUp(-1.0f);
        }

        if (press && key == GLFW.GLFW_KEY_F2) {
            this.createAndSaveScreenshot();
        }
    }

    private void updateTitle() {
        Vector3 camPosition = this.camera.getPosition();
        String title = String.format("PixelEngine - [Position] (%.3f, %.3f, %.3f) [Rotation] Pitch = %.3f, Yaw = %.3f",
            camPosition.x(), camPosition.y(), camPosition.z(),
            this.camera.getPitch(), this.camera.getYaw());
        GLFW.glfwSetWindowTitle(this.window.getHandle(), title);
    }

    public void run() {
        Callback debugCallback = GLUtil.setupDebugMessageCallback();
        if (debugCallback == null)
            log.warn("Debug mode is not available!");

        while (!this.window.shouldClose()) {
            GL30.glClear(GL30.GL_COLOR_BUFFER_BIT);
            GL30.glClearColor(0, 0, 0, 1);

            raytraceContext.draw(this.window.getWidth(), this.window.getHeight());
            this.window.swapBuffers();

            this.updateTitle();

            GLFW.glfwPollEvents();
        }

        if (debugCallback != null)
            debugCallback.free();
    }
}
