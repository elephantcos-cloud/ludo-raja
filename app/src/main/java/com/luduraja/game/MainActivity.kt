package com.luduraja.game

import android.content.Context
import android.graphics.*
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.*
import kotlin.random.Random

// ============================================================
// CONSTANTS
// ============================================================
val MAIN_PATH = listOf(
    intArrayOf(6,1),intArrayOf(6,2),intArrayOf(6,3),intArrayOf(6,4),intArrayOf(6,5),
    intArrayOf(5,6),intArrayOf(4,6),intArrayOf(3,6),intArrayOf(2,6),intArrayOf(1,6),intArrayOf(0,6),intArrayOf(0,7),
    intArrayOf(0,8),intArrayOf(1,8),intArrayOf(2,8),intArrayOf(3,8),intArrayOf(4,8),intArrayOf(5,8),
    intArrayOf(6,9),intArrayOf(6,10),intArrayOf(6,11),intArrayOf(6,12),intArrayOf(6,13),intArrayOf(6,14),intArrayOf(7,14),
    intArrayOf(8,14),intArrayOf(8,13),intArrayOf(8,12),intArrayOf(8,11),intArrayOf(8,10),intArrayOf(8,9),
    intArrayOf(9,8),intArrayOf(10,8),intArrayOf(11,8),intArrayOf(12,8),intArrayOf(13,8),intArrayOf(14,8),intArrayOf(14,7),
    intArrayOf(14,6),intArrayOf(13,6),intArrayOf(12,6),intArrayOf(11,6),intArrayOf(10,6),intArrayOf(9,6),
    intArrayOf(8,5),intArrayOf(8,4),intArrayOf(8,3),intArrayOf(8,2),intArrayOf(8,1),intArrayOf(8,0),intArrayOf(7,0)
)
val HOME_PATHS = listOf(
    listOf(intArrayOf(1,7),intArrayOf(2,7),intArrayOf(3,7),intArrayOf(4,7),intArrayOf(5,7),intArrayOf(6,7)),
    listOf(intArrayOf(7,13),intArrayOf(7,12),intArrayOf(7,11),intArrayOf(7,10),intArrayOf(7,9),intArrayOf(7,8)),
    listOf(intArrayOf(13,7),intArrayOf(12,7),intArrayOf(11,7),intArrayOf(10,7),intArrayOf(9,7),intArrayOf(8,7)),
    listOf(intArrayOf(7,1),intArrayOf(7,2),intArrayOf(7,3),intArrayOf(7,4),intArrayOf(7,5),intArrayOf(7,6))
)
val BASE_POS = listOf(
    listOf(intArrayOf(2,2),intArrayOf(2,4),intArrayOf(4,2),intArrayOf(4,4)),
    listOf(intArrayOf(2,10),intArrayOf(2,12),intArrayOf(4,10),intArrayOf(4,12)),
    listOf(intArrayOf(10,10),intArrayOf(10,12),intArrayOf(12,10),intArrayOf(12,12)),
    listOf(intArrayOf(10,2),intArrayOf(10,4),intArrayOf(12,2),intArrayOf(12,4))
)
val START_POS = intArrayOf(0, 13, 26, 39)
val HOME_ENTRY = intArrayOf(51, 12, 25, 38)
val SAFE_POS = setOf(0, 8, 13, 21, 26, 34, 39, 47)

val P_COLORS = intArrayOf(
    Color.parseColor("#E53935"), Color.parseColor("#43A047"),
    Color.parseColor("#FDD835"), Color.parseColor("#1E88E5")
)
val P_LIGHT = intArrayOf(
    Color.parseColor("#FF8A80"), Color.parseColor("#69F0AE"),
    Color.parseColor("#FFF176"), Color.parseColor("#82B1FF")
)
val P_DARK = intArrayOf(
    Color.parseColor("#B71C1C"), Color.parseColor("#1B5E20"),
    Color.parseColor("#F57F17"), Color.parseColor("#0D47A1")
)
val P_NAMES = arrayOf("লাল", "সবুজ", "হলুদ", "নীল")
val DICE_DOTS = arrayOf(
    listOf(Pair(0.5f,0.5f)),
    listOf(Pair(0.25f,0.25f),Pair(0.75f,0.75f)),
    listOf(Pair(0.25f,0.25f),Pair(0.5f,0.5f),Pair(0.75f,0.75f)),
    listOf(Pair(0.25f,0.25f),Pair(0.75f,0.25f),Pair(0.25f,0.75f),Pair(0.75f,0.75f)),
    listOf(Pair(0.25f,0.25f),Pair(0.75f,0.25f),Pair(0.5f,0.5f),Pair(0.25f,0.75f),Pair(0.75f,0.75f)),
    listOf(Pair(0.25f,0.25f),Pair(0.75f,0.25f),Pair(0.25f,0.5f),Pair(0.75f,0.5f),Pair(0.25f,0.75f),Pair(0.75f,0.75f))
)

// ============================================================
// GAME ENGINE
// ============================================================
data class Piece(val player: Int, val idx: Int, var state: String = "base", var pp: Int = -1, var hp: Int = -1)

class GameEngine {
    var np = 4; var cur = 0; var dv = 1
    var rolled = false; var over = false; var winner = -1
    val pieces = Array(4) { p -> Array(4) { i -> Piece(p, i) } }
    var movable = mutableListOf<Pair<Int,Int>>()

    fun init(n: Int) {
        np=n; cur=0; dv=1; rolled=false; over=false; winner=-1
        for(p in 0..3) for(i in 0..3) pieces[p][i] = Piece(p, i)
        movable.clear()
    }

    fun roll(): Int {
        dv = Random.nextInt(1, 7); rolled = true; findMovable(); return dv
    }

    private fun findMovable() {
        movable.clear()
        for(i in 0..3) {
            val pc = pieces[cur][i]
            when(pc.state) {
                "base" -> if(dv==6) movable.add(Pair(cur,i))
                "active" -> {
                    val d=(HOME_ENTRY[cur]-pc.pp+52)%52
                    if(d<dv) { if(dv-d-1<=5) movable.add(Pair(cur,i)) } else movable.add(Pair(cur,i))
                }
                "homepath" -> if(pc.hp+dv<=5) movable.add(Pair(cur,i))
            }
        }
    }

    fun move(pl: Int, idx: Int): Triple<Boolean,Boolean,Boolean> {
        val pc = pieces[pl][idx]; var bonus=false; var cap=false; var won=false
        when(pc.state) {
            "base" -> { pc.state="active"; pc.pp=START_POS[pl]; if(dv==6) bonus=true }
            "active" -> {
                val d=(HOME_ENTRY[pl]-pc.pp+52)%52
                if(d<dv) {
                    pc.state="homepath"; pc.pp=-1; pc.hp=dv-d-1
                    if(pc.hp>=5){ pc.hp=5; pc.state="finished"; bonus=true }
                } else {
                    pc.pp=(pc.pp+dv)%52
                    if(capture(pl,pc.pp)){ cap=true; bonus=true }
                }
            }
            "homepath" -> {
                pc.hp+=dv
                if(pc.hp>=5){ pc.hp=5; pc.state="finished"; bonus=true }
            }
        }
        if(dv==6) bonus=true
        if(pieces[pl].all{it.state=="finished"}) { over=true; winner=pl; won=true }
        rolled=false; movable.clear()
        return Triple(bonus,cap,won)
    }

    private fun capture(pl: Int, pos: Int): Boolean {
        if(SAFE_POS.contains(pos)) return false
        var c=false
        for(p in 0 until np) {
            if(p==pl) continue
            for(i in 0..3) { val e=pieces[p][i]; if(e.state=="active"&&e.pp==pos){ e.state="base"; e.pp=-1; c=true } }
        }
        return c
    }

    fun next() {
        var nxt=(cur+1)%np
        repeat(np) { if(!pieces[nxt].all{it.state=="finished"}) return@repeat; nxt=(nxt+1)%np }
        cur=nxt; rolled=false; movable.clear()
    }

    fun bestAI(): Pair<Int,Int> {
        if(movable.isEmpty()) return Pair(-1,-1)
        var best=movable[0]; var bs=-1
        for(m in movable) {
            val pc=pieces[m.first][m.second]
            val s = when(pc.state) {
                "base" -> 5
                "homepath" -> { var x=80+pc.hp; if(pc.hp+dv>=5) x=200; x }
                "active" -> {
                    val d=(HOME_ENTRY[cur]-pc.pp+52)%52
                    if(d<dv) 70 else {
                        val np2=(pc.pp+dv)%52
                        var cap=false
                        for(pp in 0 until np) { if(pp==cur) continue
                            for(i in 0..3) if(pieces[pp][i].state=="active"&&pieces[pp][i].pp==np2&&!SAFE_POS.contains(np2)) cap=true }
                        if(cap) 100 else if(SAFE_POS.contains(np2)) 40 else 20+(52-d)
                    }
                }
                else -> 0
            }
            if(s>bs){ bs=s; best=m }
        }
        return best
    }

    fun pos(pl: Int, i: Int): IntArray? {
        val pc=pieces[pl][i]
        return when(pc.state) {
            "base" -> BASE_POS[pl][i]
            "active" -> if(pc.pp in 0 until MAIN_PATH.size) MAIN_PATH[pc.pp] else null
            "homepath" -> if(pc.hp in HOME_PATHS[pl].indices) HOME_PATHS[pl][pc.hp] else null
            else -> null
        }
    }
}

// ============================================================
// LUDO VIEW
// ============================================================
class LudoView(context: Context) : View(context) {
    val eng = GameEngine()
    private val h = Handler(Looper.getMainLooper())
    private var cs = 0f; private var ox = 0f; private var oy = 0f
    var numP = 4; var started = false
    var diceVal = 1; var diceAnim = false
    var captureFlash = -1; var flashAlpha = 0f

    var onStatus: ((String) -> Unit)? = null
    var onWin: ((Int) -> Unit)? = null
    var onRollChanged: ((Boolean) -> Unit)? = null

    private val fillP = Paint(Paint.ANTI_ALIAS_FLAG)
    private val strokeP = Paint(Paint.ANTI_ALIAS_FLAG).apply { style=Paint.Style.STROKE }
    private val textP = Paint(Paint.ANTI_ALIAS_FLAG).apply { textAlign=Paint.Align.CENTER; typeface=Typeface.DEFAULT_BOLD }

    fun startGame(n: Int) { numP=n; eng.init(n); started=true; status(); invalidate() }

    private fun status() {
        val s = if(eng.cur==0) "🎯 আপনার পালা — ডাইস ফেলুন!" else "🤖 ${P_NAMES[eng.cur]} ভাবছে..."
        onStatus?.invoke(s)
        onRollChanged?.invoke(!eng.rolled && eng.cur==0 && !eng.over)
    }

    override fun onSizeChanged(w: Int, h2: Int, ow: Int, oh: Int) {
        super.onSizeChanged(w, h2, ow, oh)
        cs = minOf(w, h2) / 15f; ox = (w-cs*15)/2f; oy = (h2-cs*15)/2f
        textP.textSize = cs*0.55f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val bg = Paint(); bg.shader = LinearGradient(0f,0f,width.toFloat(),height.toFloat(),
            intArrayOf(Color.parseColor("#0F0C29"),Color.parseColor("#302B63"),Color.parseColor("#24243E")),
            null, Shader.TileMode.CLAMP)
        canvas.drawRect(0f,0f,width.toFloat(),height.toFloat(),bg)
        if(!started) return
        canvas.save(); canvas.translate(ox,oy)
        drawBoard(canvas); drawPieces(canvas)
        canvas.restore()
    }

    private fun drawBoard(canvas: Canvas) {
        for(r in 0..14) for(c in 0..14) drawCell(canvas,r,c)
        drawCenter(canvas)
        strokeP.strokeWidth=0.7f; strokeP.color=Color.parseColor("#33000000")
        for(i in 0..15) {
            canvas.drawLine(i*cs,0f,i*cs,15*cs,strokeP)
            canvas.drawLine(0f,i*cs,15*cs,i*cs,strokeP)
        }
        strokeP.strokeWidth=3f; strokeP.color=Color.parseColor("#99FFFFFF")
        val brect = RectF(0f,0f,15*cs,15*cs)
        canvas.drawRoundRect(brect, cs*0.5f, cs*0.5f, strokeP)
    }

    private fun drawCell(canvas: Canvas, r: Int, c: Int) {
        if(r in 6..8 && c in 6..8) return
        val rect = RectF(c*cs, r*cs, (c+1)*cs, (r+1)*cs)
        val inset = RectF(c*cs+2f, r*cs+2f, (c+1)*cs-2f, (r+1)*cs-2f)
        val color: Int
        val isInner: Boolean
        when {
            r in 0..5 && c in 0..5 -> { isInner=r in 1..4&&c in 1..4; color=if(isInner) Color.parseColor("#F5F5F5") else P_COLORS[0] }
            r in 0..5 && c in 9..14 -> { isInner=r in 1..4&&c in 10..13; color=if(isInner) Color.parseColor("#F5F5F5") else P_COLORS[1] }
            r in 9..14 && c in 9..14 -> { isInner=r in 10..13&&c in 10..13; color=if(isInner) Color.parseColor("#F5F5F5") else P_COLORS[2] }
            r in 9..14 && c in 0..5 -> { isInner=r in 10..13&&c in 1..4; color=if(isInner) Color.parseColor("#F5F5F5") else P_COLORS[3] }
            c==7&&r in 1..5 -> { isInner=false; color=Color.parseColor("#99E53935") }
            r==7&&c in 9..13 -> { isInner=false; color=Color.parseColor("#9943A047") }
            c==7&&r in 9..13 -> { isInner=false; color=Color.parseColor("#99FDD835") }
            r==7&&c in 1..5 -> { isInner=false; color=Color.parseColor("#991E88E5") }
            else -> {
                val pi=MAIN_PATH.indexOfFirst{it[0]==r&&it[1]==c}
                isInner=false
                color=if(pi!=-1&&SAFE_POS.contains(pi)) Color.parseColor("#22FFFFFF") else Color.parseColor("#0DFFFFFF")
            }
        }
        fillP.color=color
        if(isInner) canvas.drawRoundRect(inset, cs*0.2f, cs*0.2f, fillP)
        else canvas.drawRect(rect, fillP)

        val pi=MAIN_PATH.indexOfFirst{it[0]==r&&it[1]==c}
        if(pi!=-1&&SAFE_POS.contains(pi)) drawStar(canvas,c*cs+cs/2,r*cs+cs/2,cs*0.28f)
    }

    private fun drawCenter(canvas: Canvas) {
        val cx=7.5f*cs; val cy=7.5f*cs
        val pts = listOf(
            Pair(Path().apply{moveTo(6*cs,6*cs);lineTo(9*cs,6*cs);lineTo(cx,cy);close()}, P_COLORS[0]),
            Pair(Path().apply{moveTo(9*cs,6*cs);lineTo(9*cs,9*cs);lineTo(cx,cy);close()}, P_COLORS[1]),
            Pair(Path().apply{moveTo(6*cs,9*cs);lineTo(9*cs,9*cs);lineTo(cx,cy);close()}, P_COLORS[2]),
            Pair(Path().apply{moveTo(6*cs,6*cs);lineTo(6*cs,9*cs);lineTo(cx,cy);close()}, P_COLORS[3])
        )
        for((path,col) in pts) { fillP.color=col; canvas.drawPath(path,fillP) }
        fillP.color=Color.parseColor("#22FFFFFF"); canvas.drawCircle(cx,cy,cs*0.7f,fillP)
        fillP.color=Color.parseColor("#44FFFFFF"); canvas.drawCircle(cx,cy,cs*0.38f,fillP)
        fillP.color=Color.WHITE; canvas.drawCircle(cx,cy,cs*0.15f,fillP)
    }

    private fun drawPieces(canvas: Canvas) {
        for(p in 0 until numP) for(i in 0..3) {
            val pos=eng.pos(p,i) ?: continue
            val isMv=eng.movable.contains(Pair(p,i))
            drawPiece(canvas,p,pos[0],pos[1],isMv)
        }
    }

    private fun drawPiece(canvas: Canvas, pl: Int, r: Int, c: Int, mv: Boolean) {
        val cx=c*cs+cs/2; val cy=r*cs+cs/2; val rad=cs*0.33f
        if(mv) {
            fillP.color=Color.parseColor("#55FFFFFF")
            fillP.maskFilter=BlurMaskFilter(rad*1.5f,BlurMaskFilter.Blur.NORMAL)
            canvas.drawCircle(cx,cy,rad*2.2f,fillP)
            fillP.maskFilter=null
        }
        fillP.color=Color.parseColor("#44000000"); canvas.drawCircle(cx+1.5f,cy+2.5f,rad,fillP)
        val sh=RadialGradient(cx-rad*0.2f,cy-rad*0.25f,rad*1.2f,intArrayOf(P_LIGHT[pl],P_COLORS[pl],P_DARK[pl]),floatArrayOf(0f,0.5f,1f),Shader.TileMode.CLAMP)
        fillP.shader=sh; canvas.drawCircle(cx,cy,rad,fillP); fillP.shader=null
        fillP.color=Color.parseColor("#99FFFFFF"); canvas.drawCircle(cx-rad*0.22f,cy-rad*0.22f,rad*0.35f,fillP)
        strokeP.color=Color.parseColor("#55000000"); strokeP.strokeWidth=1.5f; canvas.drawCircle(cx,cy,rad,strokeP)
        if(mv) { strokeP.color=Color.WHITE; strokeP.strokeWidth=2.5f; canvas.drawCircle(cx,cy,rad+3f,strokeP) }
    }

    private fun drawStar(canvas: Canvas, cx: Float, cy: Float, r: Float) {
        val path=Path()
        for(i in 0..9) {
            val a=(i*PI/5-PI/2).toFloat(); val len=if(i%2==0) r else r*0.42f
            val x=cx+len*cos(a); val y=cy+len*sin(a)
            if(i==0) path.moveTo(x,y) else path.lineTo(x,y)
        }
        path.close(); fillP.color=Color.parseColor("#CCFFD700"); canvas.drawPath(path,fillP)
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        if(ev.action!=MotionEvent.ACTION_UP||!started||diceAnim||eng.over) return true
        val x=ev.x-ox; val y=ev.y-oy
        val col=(x/cs).toInt(); val row=(y/cs).toInt()
        if(row<0||row>=15||col<0||col>=15) return true
        if(eng.cur==0&&eng.rolled&&eng.movable.isNotEmpty()) {
            for(m in eng.movable) {
                val p=eng.pos(m.first,m.second) ?: continue
                if(p[0]==row&&p[1]==col) { doMove(m.first,m.second); return true }
            }
        }
        return true
    }

    fun rollDice() {
        if(eng.rolled||diceAnim||eng.cur!=0||eng.over) return
        diceAnim=true; onRollChanged?.invoke(false)
        var f=0
        val rh=Handler(Looper.getMainLooper())
        val rr=object:Runnable { override fun run() {
            diceVal=Random.nextInt(1,7); f++; invalidate()
            if(f<12) rh.postDelayed(this,60L)
            else {
                val v=eng.roll(); diceVal=v; diceAnim=false; invalidate()
                if(eng.movable.isEmpty()) {
                    onStatus?.invoke("${P_NAMES[eng.cur]} — $v পেলে, কোনো চাল নেই!")
                    h.postDelayed({ eng.next(); status(); invalidate(); if(eng.cur!=0) aiTurn() }, 1200)
                } else {
                    onStatus?.invoke("$v পেলে — গুটি বেছে নিন! 👆")
                    onRollChanged?.invoke(false)
                }
            }
        }}
        rh.post(rr)
    }

    private fun doMove(pl: Int, idx: Int) {
        val(bonus,cap,won)=eng.move(pl,idx); invalidate()
        if(won) { onWin?.invoke(pl); return }
        val msg=when { cap->"💥 গুটি কেটেছে! বোনাস পালা!"; bonus&&eng.dv==6->"🎉 ছক্কা! বোনাস পালা!"; else->"" }
        if(bonus) {
            if(msg.isNotEmpty()) onStatus?.invoke(msg)
            if(pl==0) { onRollChanged?.invoke(true); onStatus?.invoke("🎉 বোনাস পালা! আবার ডাইস ফেলুন!") }
            else h.postDelayed({ aiTurn() },800)
        } else {
            h.postDelayed({ eng.next(); status(); invalidate(); if(eng.cur!=0) aiTurn() },450)
        }
    }

    fun aiTurn() {
        if(eng.over) return
        onStatus?.invoke("🤖 ${P_NAMES[eng.cur]} ভাবছে..."); onRollChanged?.invoke(false)
        h.postDelayed({
            if(eng.over) return@postDelayed
            diceAnim=true; var f=0
            val rh=Handler(Looper.getMainLooper())
            val rr=object:Runnable { override fun run() {
                diceVal=Random.nextInt(1,7); f++; invalidate()
                if(f<9) rh.postDelayed(this,65L)
                else {
                    val v=eng.roll(); diceVal=v; diceAnim=false; invalidate()
                    if(eng.movable.isEmpty()) {
                        onStatus?.invoke("${P_NAMES[eng.cur]} — $v পেলে, কোনো চাল নেই!")
                        h.postDelayed({ eng.next(); status(); invalidate(); if(eng.cur!=0) aiTurn() },1200)
                    } else {
                        h.postDelayed({ val b=eng.bestAI(); if(b.first!=-1) doMove(b.first,b.second) },750)
                    }
                }
            }}
            rh.post(rr)
        },1100)
    }

    fun drawDice(canvas: Canvas, left: Float, top: Float, size: Float) {
        val rect=RectF(left,top,left+size,top+size)
        fillP.color=Color.WHITE
        canvas.drawRoundRect(rect,size*0.18f,size*0.18f,fillP)
        fillP.color=Color.parseColor("#22000000")
        canvas.drawRoundRect(RectF(left,top+size*0.06f,left+size,top+size+4f),size*0.18f,size*0.18f,fillP)
        fillP.color=Color.WHITE
        canvas.drawRoundRect(rect,size*0.18f,size*0.18f,fillP)
        strokeP.color=Color.parseColor("#BBBBBB"); strokeP.strokeWidth=1.5f
        canvas.drawRoundRect(rect,size*0.18f,size*0.18f,strokeP)
        fillP.color=Color.parseColor("#1A1A2E")
        val dots=DICE_DOTS.getOrNull(diceVal-1) ?: DICE_DOTS[0]
        val dotR=size*0.08f
        for((fx,fy) in dots) canvas.drawCircle(left+fx*size,top+fy*size,dotR,fillP)
    }
}

// ============================================================
// MAIN ACTIVITY
// ============================================================
class MainActivity : AppCompatActivity() {
    private lateinit var gv: LudoView
    private lateinit var statusTv: TextView
    private lateinit var rollBtn: TextView
    private lateinit var homeScr: View
    private lateinit var gameScr: View
    private lateinit var winScr: View
    private lateinit var winTv: TextView

    override fun onCreate(s: Bundle?) {
        super.onCreate(s)
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        supportActionBar?.hide()
        setContentView(buildRoot())
    }

    private fun buildRoot(): View {
        val root = FrameLayout(this)
        homeScr = buildHome(); root.addView(homeScr)
        gameScr = buildGame(); gameScr.visibility = View.GONE; root.addView(gameScr)
        winScr = buildWin(); winScr.visibility = View.GONE; root.addView(winScr)
        return root
    }

    private fun buildHome(): View {
        val scroll = ScrollView(this).apply { isFillViewport = true }
        val ll = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(40, 60, 40, 60)
            val bg = GradientDrawable().apply { shape = GradientDrawable.RECTANGLE; colors = intArrayOf(Color.parseColor("#0F0C29"), Color.parseColor("#302B63")); orientation = GradientDrawable.Orientation.TOP_BOTTOM }
            background = bg
        }

        // Logo
        val logoImg = ImageView(this).apply {
            try { val s = assets.open("logo.png"); setImageBitmap(BitmapFactory.decodeStream(s)) }
            catch (e: Exception) { setBackgroundColor(Color.parseColor("#302B63")) }
            scaleType = ImageView.ScaleType.FIT_CENTER
        }
        val lgP = LinearLayout.LayoutParams(280, 280).apply { setMargins(0,0,0,20); gravity=Gravity.CENTER }
        ll.addView(logoImg, lgP)

        // Title
        val title = TextView(this).apply {
            text = "লুডুর রাজা"; textSize = 38f; setTextColor(Color.parseColor("#F9D423"))
            gravity = Gravity.CENTER; typeface = Typeface.DEFAULT_BOLD
            setShadowLayer(12f, 0f, 3f, Color.parseColor("#FF6B35"))
        }
        ll.addView(title, mpWrap(0,0,0,6))

        val sub = TextView(this).apply { text = "🎲 খেলার রাজা হও!"; textSize=16f; setTextColor(Color.parseColor("#A78BFA")); gravity=Gravity.CENTER }
        ll.addView(sub, mpWrap(0,0,0,40))

        ll.addView(mkBtn("👤  ২ জন খেলুন", "#667EEA","#764BA2"){startGame(2)}, mpWrap(0,0,0,16))
        ll.addView(mkBtn("👥  ৪ জন খেলুন", "#F9D423","#FF6B35"){startGame(4)}, mpWrap(0,0,0,0))

        scroll.addView(ll); return scroll
    }

    private fun buildGame(): View {
        val ll = LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; setBackgroundColor(Color.parseColor("#0F0C29")) }

        // Top bar
        val top = LinearLayout(this).apply { orientation=LinearLayout.HORIZONTAL; gravity=Gravity.CENTER_VERTICAL; setPadding(12,10,12,10) }
        val back = TextView(this).apply { text="← "; textSize=22f; setTextColor(Color.WHITE); setPadding(8,6,8,6); setOnClickListener { gameScr.visibility=View.GONE; homeScr.visibility=View.VISIBLE } }
        top.addView(back)
        val ttl = TextView(this).apply { text="লুডুর রাজা 🎲"; textSize=19f; setTextColor(Color.parseColor("#F9D423")); typeface=Typeface.DEFAULT_BOLD; gravity=Gravity.CENTER }
        top.addView(ttl, LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT,1f))
        ll.addView(top)

        // Status
        statusTv = TextView(this).apply { text="আপনার পালা"; textSize=13f; setTextColor(Color.WHITE); gravity=Gravity.CENTER; setPadding(12,10,12,10)
            background = GradientDrawable().apply { setColor(Color.parseColor("#22FFFFFF")); setStroke(1,Color.parseColor("#33FFFFFF")) } }
        ll.addView(statusTv, lp(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 0, 4, 0, 4))

        // Game View
        gv = LudoView(this)
        gv.onStatus = { t -> runOnUiThread { statusTv.text = t } }
        gv.onWin = { w -> runOnUiThread { showWin(w) } }
        gv.onRollChanged = { canRoll -> runOnUiThread {
            rollBtn.isEnabled = canRoll
            rollBtn.alpha = if(canRoll) 1f else 0.5f
        }}
        ll.addView(gv, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f))

        // Roll Button
        rollBtn = mkBtn("🎲  ডাইস ফেলুন!", "#F9D423","#FF6B35") { gv.rollDice() }
        ll.addView(rollBtn, lp(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 12, 8, 12, 14))

        return ll
    }

    private fun buildWin(): View {
        val fl = FrameLayout(this).apply { setBackgroundColor(Color.parseColor("#DD000000")) }
        val card = LinearLayout(this).apply {
            orientation=LinearLayout.VERTICAL; gravity=Gravity.CENTER; setPadding(60,60,60,60)
            background=GradientDrawable().apply { shape=GradientDrawable.RECTANGLE; cornerRadius=32f; setColor(Color.parseColor("#1A1A2E")); setStroke(2,Color.parseColor("#667EEA")) }
            elevation=24f
        }
        card.addView(TextView(this).apply { text="🏆"; textSize=72f; gravity=Gravity.CENTER })
        winTv = TextView(this).apply { text="লাল জিতেছে!"; textSize=30f; setTextColor(Color.parseColor("#F9D423")); gravity=Gravity.CENTER; typeface=Typeface.DEFAULT_BOLD }
        card.addView(winTv)
        card.addView(TextView(this).apply { text="অসাধারণ খেলা! 🎉"; textSize=16f; setTextColor(Color.parseColor("#A78BFA")); gravity=Gravity.CENTER; setPadding(0,8,0,28) })
        card.addView(mkBtn("🔄  আবার খেলুন","#F9D423","#FF6B35"){ winScr.visibility=View.GONE; homeScr.visibility=View.VISIBLE })
        val flp = FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT).apply { gravity=Gravity.CENTER }
        fl.addView(card, flp); return fl
    }

    private fun mkBtn(text: String, c1: String, c2: String, onClick: ()->Unit) = TextView(this).apply {
        this.text=text; textSize=18f; setTextColor(Color.WHITE); gravity=Gravity.CENTER; typeface=Typeface.DEFAULT_BOLD
        setPadding(60,20,60,20); elevation=10f
        background=GradientDrawable().apply { shape=GradientDrawable.RECTANGLE; cornerRadius=50f; colors=intArrayOf(Color.parseColor(c1),Color.parseColor(c2)); orientation=GradientDrawable.Orientation.LEFT_RIGHT }
        setOnClickListener { onClick() }
    }

    private fun startGame(n: Int) { homeScr.visibility=View.GONE; gameScr.visibility=View.VISIBLE; winScr.visibility=View.GONE; gv.startGame(n) }
    private fun showWin(w: Int) { winTv.text = "${P_NAMES[w]} জিতেছে! 🎉"; winScr.visibility=View.VISIBLE }
    private fun mpWrap(l:Int,t:Int,r:Int,b:Int) = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { setMargins(l,t,r,b); gravity=Gravity.CENTER }
    private fun lp(w:Int,h:Int,l:Int,t:Int,r:Int,b:Int) = LinearLayout.LayoutParams(w,h).apply { setMargins(l,t,r,b) }
}
