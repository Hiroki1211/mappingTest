package executer;

public class MatchingResult {

	private SameExecutePath evoSuiteSamePathExecutePath;
	private SameExecutePath breakDownSamePathExecutePath;
	
	public MatchingResult(SameExecutePath e, SameExecutePath b) {
		evoSuiteSamePathExecutePath = e;
		breakDownSamePathExecutePath = b;
	}
	
	public SameExecutePath getEvoSuiteSamePathExecutePath() {
		return evoSuiteSamePathExecutePath;
	}
	
	public SameExecutePath getBreakDownSamePathExecutePath() {
		return breakDownSamePathExecutePath;
	}
}
