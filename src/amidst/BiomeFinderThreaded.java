package amidst;
import java.io.File;
import java.util.concurrent.*;

public class BiomeFinderThreaded {
	public static void main(String[] args) {
		int processors = Runtime.getRuntime().availableProcessors();
		System.out.println("Fyrer opp "+processors+" jobber for å lete etter himmelen....");
		ExecutorService executorService = Executors.newFixedThreadPool(processors);
		long seed = Long.parseLong(args[0]);
		long max = Integer.parseInt(args[1]);
		int perJob = 1000;
		for(long i=0;i<max;i+=perJob) {
			try {
				executorService.execute(new BiomeFinder(seed+(i*perJob), perJob, File.createTempFile("mcfinder", "tmp")));
			}catch(Exception ex) {
				ex.printStackTrace();
			}
		}
		executorService.shutdown();
	}
}
