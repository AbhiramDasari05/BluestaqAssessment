package com.bluestaq.dasaria;

import javax.swing.JButton;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;

/**
 * A custom JButton class that renders as a circle.
 * This button is non-opaque, draws its own circular background and border,
 * and only accepts clicks within its circular shape.
 */
public class CircularButton extends JButton {
    private static final int PREFERRED_SIZE = 60; // 60x60 pixels

    /**
     * Creates a circular button with a text label.
     * @param label The text or symbol to display on the button.
     */
    public CircularButton(String label) {
        super(label);

        // Set default background
        setBackground(Color.BLACK);
        setForeground(Color.WHITE);
        
        // These are important to make it draw correctly
        setOpaque(false);
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);

        // Set a fixed preferred size to make all buttons uniform and circular
        setPreferredSize(new Dimension(PREFERRED_SIZE, PREFERRED_SIZE));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        
        // Enable anti-aliasing for a smooth circle
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Set button color
        if (getModel().isArmed()) {
            // Button is pressed
            g2.setColor(Color.GRAY);
        } else {
            // Normal state
            g2.setColor(getBackground());
        }

        // Fill the circle
        g2.fillOval(0, 0, getWidth(), getHeight());

        // Draw the label (text or emoji) centered
        g2.setColor(getForeground());
        g2.setFont(getFont());
        FontMetrics fm = g2.getFontMetrics();
        int x = (getWidth() - fm.stringWidth(getText())) / 2;
        int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
        g2.drawString(getText(), x, y);
        
        g2.dispose();
    }

    @Override
    protected void paintBorder(Graphics g) {
        // Draw a circular border
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Color.DARK_GRAY); // Border color
        g2.drawOval(0, 0, getWidth() - 1, getHeight() - 1);
        g2.dispose();
    }

    @Override
    public boolean contains(int x, int y) {
        // Make the button clickable only within the circular area
        Shape shape = new Ellipse2D.Float(0, 0, getWidth(), getHeight());
        return shape.contains(x, y);
    }
}
