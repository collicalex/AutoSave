package core;

public interface Logger {

	public void logCopy(String src, String dst, long maxSrcPathLength);
	public void logSimu(String src, String dst, long maxSrcPathLength);
	public void logSkip(String src);
	public void logSave(String dir);
	
	public void logCountLabel(String src, long maxSrcPathLength);
	public void logCountValue(long value);
	
	public void logError(String text);
	public void logClear();
}
