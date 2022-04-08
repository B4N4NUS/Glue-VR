package dev.slimevr.gui;

import java.awt.Font;
import java.text.AttributedCharacterIterator.Attribute;
import java.util.Map;

/**
 * Враппер над шрифтом.
 */
public class ScalableFont extends Font {
	// Размер.
	protected float scale = 1.0f;

	// Размер при инициализации.
	protected int initSize;
	protected float initPointSize;

	/**
	 * Конструктор от атрибутов.
	 * @param attributes - атрибуты шрифта.
	 */
	public ScalableFont(Map<? extends Attribute, ?> attributes) {
		super(attributes);
		
		this.initSize = this.size;
		this.initPointSize = this.pointSize;
	}

	/**
	 * Конструктор от шрифта.
	 * @param font - шрифт.
	 */
	public ScalableFont(Font font) {
		super(font);
		
		if(font instanceof ScalableFont) {
			ScalableFont sourceFont = (ScalableFont) font;
			
			this.initSize = sourceFont.getInitSize();
			this.initPointSize = sourceFont.getInitSize2D();
			
			this.size = this.initSize;
			this.pointSize = this.initPointSize;
		} else {
			this.initSize = this.size;
			this.initPointSize = this.pointSize;
		}
	}

	/**
	 * Конструктор от шрифта и размера.
	 * @param font - шрифт.
	 * @param scale - размер.
	 */
	public ScalableFont(Font font, float scale) {
		super(font);
		
		if(font instanceof ScalableFont) {
			ScalableFont sourceFont = (ScalableFont) font;
			
			this.initSize = sourceFont.getInitSize();
			this.initPointSize = sourceFont.getInitSize2D();
		} else {
			this.initSize = this.size;
			this.initPointSize = this.pointSize;
		}
		
		setScale(scale);
	}

	/**
	 * Конструктор от имени, стиля и размера.
	 * @param name - имя шрифта.
	 * @param style - стиль шрифта.
	 * @param size - размер.
	 */
	public ScalableFont(String name, int style, int size) {
		super(name, style, size);
		
		this.initSize = this.size;
		this.initPointSize = this.pointSize;
	}

	/**
	 * Конструктор от имени, стиля, размера и скейлинга.
	 * @param name - имя шрифта.
	 * @param style - стиль шрифта.
	 * @param size - размер шрифта.
	 * @param scale - скейл.
	 */
	public ScalableFont(String name, int style, int size, float scale) {
		super(name, style, size);
		
		this.initSize = this.size;
		this.initPointSize = this.pointSize;
		
		setScale(scale);
	}


	public int getInitSize() {
		return initSize;
	}
	
	public float getInitSize2D() {
		return initPointSize;
	}
	
	public float getScale() {
		return scale;
	}

	/**
	 * Установка нового размера шрифта.
	 * @param scale - размер.
	 */
	private void setScale(float scale) {
		this.scale = scale;
		
		float newPointSize = initPointSize * scale;
		
		this.size = (int) (newPointSize + 0.5);
		this.pointSize = newPointSize;
	}
}
