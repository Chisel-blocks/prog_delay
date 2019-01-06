
// Dsp-block prog_delay
// Description here 
// Inititally written by dsp-blocks initmodule.sh, 20190106
package prog_delay

import chisel3.experimental._
import chisel3._
import dsptools._
import dsptools.numbers._
import breeze.math.Complex

class prog_delay_io[T <:Data](proto: T,n: Int) 
   extends Bundle {
        val A       = Input(Vec(n,proto))
        val B       = Output(Vec(n,proto))
        override def cloneType = (new prog_delay_io(proto.cloneType,n)).asInstanceOf[this.type]
   }

class prog_delay[T <:Data] (proto: T,n: Int) extends Module {
    val io = IO(new prog_delay_io( proto=proto, n=n))
    val register=RegInit(VecInit(Seq.fill(n)(0.U.asTypeOf(proto.cloneType))))
    register:=io.A
    io.B:=register
}

//This gives you verilog
object prog_delay extends App {
    chisel3.Driver.execute(args, () => new prog_delay(
        proto=DspComplex(UInt(16.W),UInt(16.W)), n=8) 
    )
}
