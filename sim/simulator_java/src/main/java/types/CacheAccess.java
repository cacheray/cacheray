package types;

public class CacheAccess{
	public static final int READ = 0;
	public static final int WRITE = 1;

	private boolean stradledCacheline;
	private int[] hits; 
	private int misses; 
	private int levelTracker;
	private int nCaches;
	private int type;

	public CacheAccess(int nCaches, int type){
		this.nCaches = nCaches;
		hits = new int[nCaches];
		misses = 0;
		stradledCacheline = false;
		levelTracker = 0;
		this.type = type;
	}

	public CacheAccess(int nCaches) {
		this.nCaches = nCaches;
		hits = new int[nCaches];
		misses = 0;
		stradledCacheline = false;
		levelTracker = 0;
		this.type = READ;
	}

	public void hit(){
		hits[levelTracker]++;
	}

	public int getHits(int level){
		if(level < 0 || level >= nCaches){
			return 0;
		}
		//System.out.println(hits[level]);
		return hits[level];
	}

	public void miss(){
		misses++;
	}

	public int getMisses(){
		return misses;
	}

	public void setStradle(boolean stradles){
		stradledCacheline = stradles;
	}

	public boolean getStradle(){
		return stradledCacheline;
	}

	public void increaseLevel(){
		levelTracker++;
	}

	public void decreaseLevel(){
		levelTracker--;
	}

	public boolean isRead() {
		return type == READ;
	}

	public void print(){
		System.out.print("CA contains following hit stats:");
		for (int i : hits){
			System.out.print(" " +i+", ");

		System.out.println("");
		}
	}
}