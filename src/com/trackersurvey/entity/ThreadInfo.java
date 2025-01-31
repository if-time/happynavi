/*
 * @Title ThreadInfo.java
 * @Copyright Copyright 2010-2015 Yann Software Co,.Ltd All Rights Reserved.
 * @Description：
 * @author Yann
 * @date 2015-8-7 下午10:16:34
 * @version 1.0
 */
package com.trackersurvey.entity;



/** 
 * 线程信息
 * @author Yann
 * @date 2015-8-7 下午10:16:34
 */
public class ThreadInfo
{
	private int id;
	private String url;
	private int start;
	private int end;
	private int finished;
	
	public ThreadInfo()
	{
	}
	
	/** 
	 *@param id
	 *@param url
	 *@param start
	 *@param end
	 *@param finished
	 */
	public ThreadInfo(int id, String url, int start, int end, int finished)
	{
		this.id = id;
		this.url = url;
		this.start = start;
		this.end = end;
		this.finished = finished;
	}
	public int getId()
	{
		return id;
	}
	public void setId(int id)
	{
		this.id = id;
	}
	public String getUrl()
	{
		return url;
	}
	public void setUrl(String url)
	{
		this.url = url;
	}
	public int getStart()
	{
		return start;
	}
	public void setStart(int start)
	{
		this.start = start;
	}
	public int getEnd()
	{
		return end;
	}
	public void setEnd(int end)
	{
		this.end = end;
	}
	public int getFinished()
	{
		return finished;
	}
	public void setFinished(int finished)
	{
		this.finished = finished;
	}
	@Override
	public String toString()
	{
		return "ThreadInfo [id=" + id + ", url=" + url + ", start=" + start
				+ ", end=" + end + ", finished=" + finished + "]";
	}
	
}
