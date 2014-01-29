package com.gravspace.util;

import static org.junit.Assert.*;

import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.junit.Test;

public class TestFormParser {

	@Test
	public void testFormParser(){
		List<NameValuePair> formElements = new ArrayList<NameValuePair>();
		formElements.add(new BasicNameValuePair("stringElement", "string"));
		formElements.add(new BasicNameValuePair("listOfStringElements", "string1"));
		formElements.add(new BasicNameValuePair("listOfStringElements", "string2"));
		formElements.add(new BasicNameValuePair("intElement", "1"));
		formElements.add(new BasicNameValuePair("listOfIntElements", "1"));
		formElements.add(new BasicNameValuePair("listOfIntElements", "2"));
		
	    //Class<?> subClass = (Class<?>) pt.getActualTypeArguments()[0];
	    //System.out.println(subClass.getCanonicalName());
		
		FormBean fb = FormUtil.map(formElements, new FormBean());
		assertEquals("string", fb.getStringElement());
		assertEquals("string1", fb.getListOfStringElements().get(0));
		assertEquals("string2", fb.getListOfStringElements().get(1));
		assertEquals(new Integer(1), fb.getIntElement());
		assertEquals(new Integer(1), fb.getListOfIntElements().get(0));
		
	}
	
	public static class FormBean {
		String stringElement;
		List<String> listOfStringElements;
		Integer intElement;
		List<Integer> listOfIntElements;
		
		public String getStringElement() {
			return stringElement;
		}
		public void setStringElement(String stringElement) {
			this.stringElement = stringElement;
		}
		public List<String> getListOfStringElements() {
			return listOfStringElements;
		}
		public void setListOfStringElements(List<String> listOfStringElements) {
			this.listOfStringElements = listOfStringElements;
		}
		public Integer getIntElement() {
			return intElement;
		}
		public void setIntElement(Integer intElement) {
			this.intElement = intElement;
		}
		public List<Integer> getListOfIntElements() {
			return listOfIntElements;
		}
		public void setListOfIntElements(List<Integer> listOfIntElements) {
			this.listOfIntElements = listOfIntElements;
		}
	}
	
}
