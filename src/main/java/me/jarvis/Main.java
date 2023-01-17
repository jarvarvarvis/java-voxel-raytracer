package me.jarvis;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class Main {
    public static final Logger log = LogManager.getLogger(Main.class);

	public static void main(String[] args) throws IOException {
        PixelEngine.init();
        log.info("Engine initialization complete");
		PixelEngine engine = new PixelEngine(1600, 900);
        log.info("Engine context initialized, running...");
        engine.run();
	}
}