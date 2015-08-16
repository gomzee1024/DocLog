package com.doctorlog.gautamkakadiya.doctorlog;

import android.database.Cursor;

public class MyListItem{
  private String name;
  private String date;
  
  public void setName(String name,String date){
    this.name=name;
    this.date=date;
  }
  public String getName(){
    return name;
  }

  public String getDate(){
    return date;
  }


}