package core;

public interface Logger {

	public void logCopy(String src, String dst, long maxSrcPathLength);
	public void logSimu(String src, String dst, long maxSrcPathLength);
	public void logSkip(String src);
	
	public void logText(String text);
	public void logError(String text);
	public void logClear();
}
