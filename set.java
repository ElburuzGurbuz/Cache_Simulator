public class set
{
	// her setin içinde minline olacak her set E tane line
	int E = 0;
	line line[] = null;

	int lineindexwithmintime = 0;
	public set(int E)
	{
		this.E = E;
		this.line = new line[E];

		for (int i = 0; i < E; i++)
		{
			this.line[i] = new line(null, 0, 0, false, i);
		}
	}
}