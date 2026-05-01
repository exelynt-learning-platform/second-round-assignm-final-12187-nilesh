package com.nv.ecommerce.practice;


public class Frequency {
	public static void main(String[] args) {
		
		int arr[] = new int[] {10,20,10,20,30,10};
		
		int brr[] = new int[100];
		
		for(int i= 0; i < arr.length; i++)
		{
			brr[arr[i]]++;
		}
		
		for(int i = 0; i < arr.length; i++)
		{
			System.out.println("frequency of :" + arr[i] + " ----"+ brr[arr[i]]);
		}
	}
}
