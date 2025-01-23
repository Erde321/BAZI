package de.uni.augsburg.bazi.gui;

import java.awt.Point;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

public class BaziProperties extends Properties
{

	public static final String filename = "config.xml";

	private static final long serialVersionUID = 1L;

	public BaziProperties()
	{
		try
		{
			loadFromXML(new FileInputStream(filename));
		}
		catch (Exception e)
		{}
	}

	public void save()
	{
		try
		{
			storeToXML(new FileOutputStream(filename), "BAZI configuration file2");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public Point getPoint(String key)
	{
		String value = getProperty(key);
		if (value == null || !value.matches("\\d+,\\d+"))
			return null;
		String[] split = value.split(",");
		return new Point(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
	}

	public void setPoint(String key, Point p)
	{
		setProperty(key, String.format("%s,%s", p.x, p.y));
	}

	public Boolean getBoolean(String key)
	{
		String value = getProperty(key);
		if (value == null || !(value.equals("1") || value.equals("0")))
			return null;
		return value.equals("1");
	}

	public void setBoolean(String key, Boolean b)
	{
		setProperty(key, b ? "1" : "0");
	}

	public Integer getInteger(String key)
	{
		String value = getProperty(key);
		if (value == null || !value.matches("\\d+"))
			return null;
		return Integer.parseInt(value);
	}

	public void setInteger(String key, Integer i)
	{
		setProperty(key, i + "");
	}
}
