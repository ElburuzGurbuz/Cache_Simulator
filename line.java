public class line
{
	String data = null;
	int tag = 0;
	int time = 0;
	boolean v = false;
	int index;

	public line(String data, int tag, int time, boolean v, int index)
	{
		this.data = data;
		this.tag = tag;
		this.time = time;
		this.v = v;
		this.index = index;
	}

}