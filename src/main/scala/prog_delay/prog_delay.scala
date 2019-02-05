// See LICENSE for license details.
//
//Start with a static tb and try to genererate a gnerator for it
package prog_delay
import chisel3._
import chisel3.util._
import chisel3.experimental._
import dsptools._
import dsptools.numbers._


class prog_delay_io[ T <: Data]( proto: T, val maxdelay: Int=64) extends Bundle {
   requireIsChiselType(proto.cloneType)
   val iptr_A= Input(proto.cloneType)
   val optr_Z= Output(proto.cloneType)
   val select= Input(UInt(log2Ceil(maxdelay).W))
   override def cloneType = (new prog_delay_io(proto.cloneType,maxdelay)).asInstanceOf[this.type]
}


//Class of programmable
class prog_delay[ T <: Data ] (val proto: T, val maxdelay: Int=64)extends Module{
    val io=IO(
        new prog_delay_io(proto.cloneType,maxdelay)
       )
    // Both work, but asTypeOf should provide less "problems"
    //val zero=Ring[T].zero
    val zero=0.U.asTypeOf(proto.cloneType)
    val inreg=RegInit(zero)
    inreg:=io.iptr_A
    val stage=Wire(Vec(log2Ceil(maxdelay)+1,proto.cloneType))
    val stageo=Wire(Vec(log2Ceil(maxdelay),proto.cloneType))
    stage(0):=inreg
    val i=Seq.range(0,log2Ceil(maxdelay))
    val pipes=i.map(i=>Module(new Pipe(proto.cloneType,latency=scala.math.pow(2,i).toInt)).io)

    for ( index <- 0 to log2Floor(maxdelay) ){
        pipes(index).enq.valid:=true.B
        pipes(index).enq.bits:=stage(index)
        stageo(index):=pipes(index).deq.bits
        stage(index+1):=Mux(io.select(index),stageo(index),stage(index))
    }

    val oreg=RegInit(zero)
    oreg:=stage(log2Floor(maxdelay))
    io.optr_Z:=oreg
}


object prog_delay extends App {
  val n=15
  val proto=DspComplex(SInt(n.W), SInt(n.W))
  chisel3.Driver.execute(args, () => new prog_delay( proto=proto.cloneType, maxdelay=63
  ))
}

