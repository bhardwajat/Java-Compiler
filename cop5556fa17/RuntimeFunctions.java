package cop5556fa17;

public class RuntimeFunctions {
//	KW_cart_x/* cart_x */, KW_cart_y/* cart_y */, KW_polar_a/* polar_a */, KW_polar_r/* polar_r */, 
//	KW_abs/* abs */, KW_sin/* sin */, KW_cos/* cos */, KW_atan/* atan */, KW_log/* log */, 
	
	//You do not need to implement sin, cos, or atan.

	public static String className = "cop5556fa17/RuntimeFunctions";

	public static String absSig = "(I)I";
	public static int abs(int arg0) {
		return Math.abs(arg0);
	}
	
	public static String polar_aSig = "(II)I";
	public static int polar_a(int x, int y) {
		double  a=  (int) Math.toDegrees( Math.atan2(y, x) );
		//System.out.println("polar_a(" + x + ", " + y + ") -> a = " + (int) Math.round(a));
		return (int) a;
	}

	public static String polar_rSig = "(II)I";
	public static int polar_r(int x, int y) {
		double  r = (int) Math.hypot(x,y);
		//System.out.println("polar_r(" + x + ", " + y + ") -> r = " + (int) Math.round(r));
		return (int)r;
	}
	
	public static String cart_xSig = "(II)I";
	public static int cart_x(int r, int theta) {
		double thetaR = Math.cos(Math.toRadians(theta));
		double x =  r * Math.cos(thetaR);
		//System.out.println("cart_x(" + r + ", " + theta + ") -> x = " + (int) Math.round(x));
		return  (int)x;

	}
	
	public static String cart_ySig = "(II)I";
	public static int cart_y(int r, int theta) {
		double y =  r * Math.sin(Math.toRadians(theta));
		//System.out.println("cart_y(" + r + ", " + theta + ") -> y = " + (int) Math.round(y));
		return (int) y;
	}
	
	public static final String logSig = "(I)I";
	public static int log(int arg0) {
		double l =  Math.round(Math.log(arg0));
		return (int) l;
	}
	
	
}
