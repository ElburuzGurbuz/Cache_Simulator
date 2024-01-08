public class trace
{
	String address = null;
	String operation = null;
	String size = null;
	String data = null;
	public trace(String operation, String address, String size, String data)
	{
		this.address = address;
		this.operation = operation;
		this.size = size;
		this.data = data;

	}
	public static char[] convertToBinary(String hexaDecimal, int arrayDimension)
	{
		char[] newStr = new char[arrayDimension];
		int index = arrayDimension - 1;
		for (int i = hexaDecimal.length() - 1; i >= 0; i--)
		{

			int numberOfDigit = 0;
			int bit = 0;
			if (hexaDecimal.charAt(i) >= 48 && hexaDecimal.charAt(i) <= 57)
			{
				bit = hexaDecimal.charAt(i) % 48;
			}
			else if (hexaDecimal.charAt(i) == 32)
			{
				continue;
			}
			else
			{
				bit = hexaDecimal.charAt(i) % 87;
			}

			int division = bit;
			int remainder = 0;
			if (bit >= 2)
			{
				division = bit / 2;

				remainder = bit % 2;

				newStr[index] = ((char) (remainder + 48));
				numberOfDigit++;
				index--;
			}

			for (int j = 1; j > 0; j++)
			{
				if (division >= 2)
				{
					remainder = division % 2;
					newStr[index] = ((char) (remainder + 48));
					index--;
					numberOfDigit++;
				}
				if (division >= 2)
				{
					division = division / 2;
				}
				else
				{
					newStr[index] = ((char) (division + 48));

					index--;
					numberOfDigit++;

					while (numberOfDigit < 4)
					{
						newStr[index] = ((char) (48));
						index--;
						numberOfDigit++;
					}
					if (index != 0)
					{

						break;
					}
					if (index == 0)
					{
						break;
					}
				}

			}

		}
		return newStr;
	}

}