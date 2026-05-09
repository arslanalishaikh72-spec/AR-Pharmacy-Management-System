package arpharmacy;

import java.awt.*;

/**
 * Central colour / font palette for AR Pharmacy System.
 * Every panel references these constants so the whole app stays consistent.
 */
public class Theme {

    // ── Palette ──────────────────────────────────────────────────────────────
    public static final Color PRIMARY       = new Color(0,  150,  90);   // emerald green
    public static final Color PRIMARY_DARK  = new Color(0,  110,  65);
    public static final Color PRIMARY_LIGHT = new Color(220, 255, 240);
    public static final Color ACCENT        = new Color(30,  110, 200);  // trust blue
    public static final Color DANGER        = new Color(210,  45,  45);
    public static final Color WARNING       = new Color(240, 150,  20);

    public static final Color BG_MAIN       = new Color(245, 250, 248);
    public static final Color BG_CARD       = Color.WHITE;
    public static final Color BG_HEADER     = new Color(0,  140,  80);

    public static final Color TEXT_DARK     = new Color( 30,  40,  35);
    public static final Color TEXT_MID      = new Color( 90, 105,  98);
    public static final Color TEXT_LIGHT    = new Color(200, 215, 208);
    public static final Color TEXT_WHITE    = Color.WHITE;

    public static final Color BORDER        = new Color(200, 225, 215);
    public static final Color TABLE_EVEN    = new Color(240, 250, 245);
    public static final Color TABLE_ODD     = Color.WHITE;
    public static final Color TABLE_HEADER  = new Color(0,  130,  75);

    // ── Fonts ────────────────────────────────────────────────────────────────
    public static final Font FONT_TITLE     = new Font("Segoe UI", Font.BOLD,  22);
    public static final Font FONT_SUBTITLE  = new Font("Segoe UI", Font.BOLD,  15);
    public static final Font FONT_BODY      = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL     = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_BTN       = new Font("Segoe UI", Font.BOLD,  13);
    public static final Font FONT_FIELD     = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_TABLE_HDR = new Font("Segoe UI", Font.BOLD,  12);
    public static final Font FONT_TABLE     = new Font("Segoe UI", Font.PLAIN, 12);

    // ── Sizes ────────────────────────────────────────────────────────────────
    public static final Dimension BTN_SIZE  = new Dimension(160, 36);
    public static final int       RADIUS    = 10;

    private Theme() {}
}
