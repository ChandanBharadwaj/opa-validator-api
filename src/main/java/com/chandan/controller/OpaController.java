package com.chandan.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OpaController {
	private static class StreamGobbler implements Runnable {
		private InputStream inputStream;
		private Consumer<String> consumer;

		public StreamGobbler(InputStream inputStream, Consumer<String> consumer) {
			this.inputStream = inputStream;
			this.consumer = consumer;
		}

		@Override
		public void run() {
			new BufferedReader(new InputStreamReader(inputStream)).lines().forEach(consumer);
		}
	}

	Logger log = LoggerFactory.getLogger(this.getClass());


	@PostMapping(value = "/api/opa/rego/validate", consumes = MediaType.TEXT_PLAIN_VALUE)
	public ResponseEntity<OpaResponse> validate(@RequestBody String regoText) throws IOException, InterruptedException {
		String rego = StringEscapeUtils.unescapeJson(regoText);
		String id = UUID.randomUUID().toString();
		String fileName = createRegoFile(rego, id);

		ProcessBuilder builder = new ProcessBuilder();
		builder.command("opa", "test", fileName);
		Process process = builder.start();
		StreamGobbler streamGobbler = new StreamGobbler(process.getInputStream(), log::debug);
		Executors.newSingleThreadExecutor().submit(streamGobbler);
		int exitCode = process.waitFor();
		log.debug("\nExited with error code : " + exitCode);
		List<String> results;
		if (exitCode == 0) {
			results = readOutput(process.getInputStream());
		} else {
			results = readOutput(process.getErrorStream());
		}
		results.forEach(log::debug);
		
		File fileToDelete = FileUtils.getFile(fileName);
		boolean success = FileUtils.deleteQuietly(fileToDelete);
		if(success) {			
			log.debug(fileName + " File Deleted");
		}
		if (results.isEmpty()) {
			return ResponseEntity.ok(new OpaResponse("Valid Rego"));
		} else {
			return ResponseEntity.ok(new OpaResponse(results));
		}

	}

	private List<String> readOutput(InputStream inputStream) throws IOException {
		try (BufferedReader output = new BufferedReader(new InputStreamReader(inputStream))) {
			return output.lines().collect(Collectors.toList());
		}
	}

	private String createRegoFile(String rego, String id) {
		String filename = id + ".rego";
		try {
			FileWriter myWriter = new FileWriter(filename);
			myWriter.write(rego);
			myWriter.close();
		} catch (IOException e) {
			log.error(e.getMessage());
		}
		return filename;
	}
}
