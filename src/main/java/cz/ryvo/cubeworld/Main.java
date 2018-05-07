package cz.ryvo.cubeworld;

import lombok.extern.slf4j.Slf4j;
import org.lwjgl.Version;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.APIUtil;

import java.util.Map;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

@Slf4j
public class Main {

  // The window handle
  private long window;

  public void run() {
    log.debug("LWJGL version: {}", Version.getVersion());
    init();
    loop();
  }

  private void init() {
    // Setup an error callback. The default implementation will print the error message in System.err.
    GLFWErrorCallback.create(new GLFWErrorCallback() {
      private Map<Integer, String> ERROR_CODES = APIUtil.apiClassTokens((field, value) -> 0x10000 < value && value < 0x20000, null, GLFW.class);

      public void invoke(int error, long description) {
        String msg = getDescription(description);
        Exception exception = new RuntimeException();
        exception.setStackTrace(Thread.currentThread().getStackTrace());
        log.error(String.format("LWJGL error %s - %s", ERROR_CODES.get(error), msg), exception);
      }
    });

    // Initialize GLFW. Most GLFW functions will not work before doing this.
    if (!glfwInit()) {
      throw new IllegalStateException("Unable to initialize GLFW");
    }

    // Configure our window
    glfwDefaultWindowHints(); // optional, the current window hints are already the default
    glfwWindowHint(GLFW_VISIBLE, GL_FALSE); // the window will stay hidden after creation
    glfwWindowHint(GLFW_RESIZABLE, GL_FALSE); // the window will not be resizable

    int WIDTH = 1024;
    int HEIGHT = 768;

    // Create the window
    window = glfwCreateWindow(WIDTH, HEIGHT, "Hello World!", NULL, NULL);
    if (window == NULL) {
      throw new RuntimeException("Failed to create the GLFW window");
    }

    // Setup a key callback. It will be called every time a key is pressed, repeated or released.
    glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
      if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
        glfwSetWindowShouldClose(window, true); // We will detect this in the rendering loop
      }
    });

    // Get the resolution of the primary monitor
    GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
    // Center our window
    glfwSetWindowPos(
        window,
        (vidmode.width() - WIDTH) / 2,
        (vidmode.height() - HEIGHT) / 2
    );

    // Make the OpenGL context current
    glfwMakeContextCurrent(window);
    // Enable v-sync
    glfwSwapInterval(1);

    // Make the window visible
    glfwShowWindow(window);
  }

  private void loop() {
    // This line is critical for LWJGL's interoperation with GLFW's
    // OpenGL context, or any context that is managed externally.
    // LWJGL detects the context that is current in the current thread,
    // creates the ContextCapabilities instance and makes the OpenGL
    // bindings available for use.
    GL.createCapabilities();

    // Set the clear color
    glClearColor(1.0f, 0.0f, 0.0f, 0.0f);

    // Run the rendering loop until the user has attempted to close
    // the window or has pressed the ESCAPE key.
    while (!glfwWindowShouldClose(window)) {
      glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer

      glfwSwapBuffers(window); // swap the color buffers

      // Poll for window events. The key callback above will only be
      // invoked during this call.
      glfwPollEvents();
    }
  }

  public static void main(String[] args) {
    new Main().run();
  }
}
