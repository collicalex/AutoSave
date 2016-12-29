package core;

public class LoggerAdapter implements Logger {

	private Logger _logger = null;
	
	public void setLogger(Logger logger) {
		_logger = logger;
	}

	@Override
	public void logCopy(String src, String dst, long maxSrcPathLength) {
		if (_logger != null) {
			_logger.logCopy(src, dst, maxSrcPathLength);
		}
	}

	@Override
	public void logSimu(String src, String dst, long maxSrcPathLength) {
		if (_logger != null) {
			_logger.logSimu(src, dst, maxSrcPathLength);
		}
	}
	
	@Override
	public void logSkip(String src) {
		if (_logger != null) {
			_logger.logSkip(src);
		}
	}

	@Override
	public void logText(String text) {
		if (_logger != null) {
			_logger.logText(text);
		}
	}

	@Override
	public void logError(String text) {
		if (_logger != null) {
			_logger.logError(text);
		}
	}

	@Override
	public void logClear() {
		if (_logger != null) {
			_logger.logClear();
		}
	}
	
}
