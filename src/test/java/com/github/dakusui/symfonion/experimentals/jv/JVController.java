package com.github.dakusui.symfonion.experimentals.jv;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Receiver;
import javax.sound.midi.SysexMessage;

import static com.github.dakusui.symfonion.compat.exceptions.CompatExceptionThrower.deviceException;


public class JVController {
	public synchronized void sendexeclusive() throws Exception {
		MidiDevice midiout = getMidiOutDevice();
		midiout.open();
		try {
			Receiver rec = midiout.getReceiver();
			try {
				//rec.send(createDataSetSysExMessage(0x00000000, (byte)0), -1);
				//rec.send(createDataReqSysExMessage(0x11001002, (byte)00, (byte)00, (byte)00, (byte)01), -1);
				//
				//rec.send(createDataSetSysExMessage(0x00000004, (byte)0x0c, (byte)0x08), 100);
				rec.send(createDataSetSysExMessage(0, (byte)0x00), -1);
				rec.send(createDataSetSysExMessage(1, (byte)0x08), -1);
				//rec.send(createDataSetSysExMessage(0x11001000, (byte)0x01), -1);
				//rec.send(createDataSetSysExMessage(0x11001001, (byte)0x02), -1);
				//rec.send(createDataSetSysExMessage(0x11001002, (byte)0x01), -1);
				//rec.send(createDataSetSysExMessage(0x11001003, (byte)0x00, (byte)0x03), -1);
				//rec.send(createDataSetSysExMessage(0x11001065, (byte)0x10), -1);
				//rec.send(createDataSetSysExMessage(0x11001265, (byte)0x10), -1);
				//rec.send(createDataSetSysExMessage(0x11001465, (byte)0x10), -1);
				//rec.send(createDataSetSysExMessage(0x11001665, (byte)0x10), -1);
				//rec.send(createDataSetSysExMessage(0x11001200, (byte)0x01), -1);
				//rec.send(createDataSetSysExMessage(0x11001400, (byte)0x01), -1);
				//rec.send(createDataSetSysExMessage(0x11001600, (byte)0x01), -1);
				//rec.send(createShortMessage(), -1);
			} finally {
				rec.close();
			}
		} catch (Exception e) {
			throw deviceException("Failed to access a midi device.", e);
		} finally {
			midiout.close();
		}
	}

	MidiMessage createDataSetSysExMessage(int address, byte... body) throws InvalidMidiDataException, IOException {
		SysexMessage ret = new SysexMessage();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		baos.write(SysexMessage.SYSTEM_EXCLUSIVE);    // status: SysEx start
		baos.write(0x41);    // Roland
		baos.write(0x10);    // Device ID
		baos.write(0x6a);    // JV-1010
		baos.write(0x12);    // DT-1
		int sum = 0;
		int d = 0;
		baos.write(d = ((address & 0xff000000) >> 24));  sum += d; System.out.println("1:" + d + "(address)");
		baos.write(d = ((address & 0x00ff0000) >> 16));  sum += d; System.out.println("2:" + d + "(address)");
		baos.write(d = ((address & 0x0000ff00) >> 8));   sum += d; System.out.println("3:" + d + "(address)");
		baos.write(d =  (address & 0x000000ff));         sum += d; System.out.println("4:" + d + "(address)");
		int i = 5;
		for (byte b : body) {
			baos.write(b);
			sum += b;
			System.out.println(i ++ + ":" + b + "(data)");
		}
		int checksum = (128 - (sum % 128)) % 128;
		baos.write(checksum);
		System.out.println(i + ":" + checksum + "(checksum)");
		baos.write(SysexMessage.SPECIAL_SYSTEM_EXCLUSIVE);    // End of exclusive 
		//HexDumpEncoder dumper = new HexDumpEncoder();
		//System.out.println(dumper.encode(baos.toByteArray()));
		//System.out.println(baos.size());
		baos.close();
		ret.setMessage(
				baos.toByteArray(),
				baos.size()
		);
		return ret;
	}

	MidiMessage createDataReqSysExMessage(int address, byte... body) throws InvalidMidiDataException, IOException {
		SysexMessage ret = new SysexMessage();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		baos.write(SysexMessage.SYSTEM_EXCLUSIVE);    // status: SysEx start
		baos.write(0x41);    // Roland
		baos.write(0x10);    // Device ID
		baos.write(0x6a);    // JV-1010
		baos.write(0x11);    // Data request
		int sum = 0;
		int d = 0;
		baos.write(d = ((address & 0xff000000) >> 24));  sum += d; System.out.println("1:" + d + "(address)");
		baos.write(d = ((address & 0x00ff0000) >> 16));  sum += d; System.out.println("2:" + d + "(address)");
		baos.write(d = ((address & 0x0000ff00) >> 8));   sum += d; System.out.println("3:" + d + "(address)");
		baos.write(d =  (address & 0x000000ff));         sum += d; System.out.println("4:" + d + "(address)");
		for (byte b : body) {
			baos.write(b);
			sum += b;  System.out.println("i:" + d);
		}
		int checksum = (128 - (sum % 128)) % 128;
		System.out.println("sum:" + checksum);
		baos.write(checksum);
		baos.write(SysexMessage.SPECIAL_SYSTEM_EXCLUSIVE);    // End of exclusive
		baos.close();
		//HexDumpEncoder dumper = new HexDumpEncoder();
		//System.out.println(dumper.encode(baos.toByteArray()));
		//System.out.println(baos.size());
		ret.setMessage(
				baos.toByteArray(),
				baos.size()
		);
		return ret;
	}
	MidiDevice getMidiOutDevice() {
		MidiDevice ret = null;
		MidiDevice.Info[] infoItems = MidiSystem.getMidiDeviceInfo();
		for (MidiDevice.Info info : infoItems) {
			System.out.println("   <" + info + ">: vendor:(" + info.getVendor() + "), name:(" + info.getName() + ") , desc:(" + info.getDescription() + ")");
			if (info.getName().startsWith("UM1")) {
				Object tmp = null;
				MidiDevice dev = null;
				try {
					dev = MidiSystem.getMidiDevice(info);
					dev.open();
					try {
						tmp = dev.getReceiver();
					} finally {
						dev.close();
					}
				} catch (Exception e) {
				}
				System.out.println("    ..." + tmp);
				if (tmp != null) {
					ret = dev;
					break;
				}
			}
		}
		System.out.println("target device=<" + ret + ">");
		return ret;
	}	

	MidiDevice _getMidiOutDevice() {
		MidiDevice ret = null;
		MidiDevice.Info[] infoItems = MidiSystem.getMidiDeviceInfo();
		for (MidiDevice.Info info : infoItems) {
			if ("EDIROL".equals(info.getVendor())) {
				Object tmp = null;
				MidiDevice dev = null;
				try {
					dev = MidiSystem.getMidiDevice(info);
					dev.open();
					try {
						tmp = dev.getReceiver();
					} finally {
						dev.close();
					}
				} catch (Exception e) {
				}
				if (tmp != null) {
					ret = dev;
					break;
				}
			}
		}
		return ret;
	}

	MidiDevice getMidiInDevice() {
		MidiDevice ret = null;
		MidiDevice.Info[] infoItems = MidiSystem.getMidiDeviceInfo();
		for (MidiDevice.Info info : infoItems) {
			if ("EDIROL".equals(info.getVendor())) {
				Object tmp = null;
				MidiDevice dev = null;
				try {
					dev = MidiSystem.getMidiDevice(info);
					dev.open();
					try {
						tmp = dev.getReceiver();
					} finally {
						dev.close();
					}
				} catch (Exception e) {
				}
				if (tmp == null) {
					ret = dev;
					break;
				}
			}
		}
		return ret;
	}
	
	public static void main(String[] args) throws Exception {
		new JVController().sendexeclusive();
	}
}
