package com.gnome.gnome.switcher.switcherPage.component;

import javafx.scene.layout.AnchorPane;
import javafx.scene.transform.Scale;


/**
 * The Component class provides utility methods for handling components in the application.
 * One of its key functionalities is to resize an AnchorPane by applying a scale transform.
 */
public class Component {
     static public void resize(AnchorPane comp, double width, double height){
         double scaleX= width/comp.getPrefWidth();
         double scaleY=height/comp.getPrefHeight();
         comp.getTransforms().add(new Scale(scaleX,scaleY,0,0));
     }
}
