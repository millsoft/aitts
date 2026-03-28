package com.millsoft.aitts.global;

import android.media.AudioFormat;
import android.content.Context;
import android.util.Base64;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import javazoom.jl.converter.Converter;
import javazoom.jl.decoder.Decoder;
import javazoom.jl.decoder.Header;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.decoder.Obuffer;

public class Audio {

    private static final String WAV_FILENAME = "out.wav";

    private Context context;

    public Audio(Context context) {
        this.context = context;
    }

    public static final class DecodedAudio {
        public final byte[] pcmData;
        public final int sampleRateHz;
        public final int channelCount;
        public final int audioFormat;

        public DecodedAudio(byte[] pcmData, int sampleRateHz, int channelCount, int audioFormat) {
            this.pcmData = pcmData;
            this.sampleRateHz = sampleRateHz;
            this.channelCount = channelCount;
            this.audioFormat = audioFormat;
        }
    }

    public DecodedAudio getPcmFromBase64Wav(String string) {
        byte[] wavBytes = Base64.decode(string, Base64.DEFAULT);
        Log.d("Audio", "decoded wav " + wavBytes.length + " bytes");
        return parseWav(wavBytes);
    }

    public byte[] getAudioStreamFromString(String string) {

        byte[] mp3bytes = Base64.decode(string, Base64.DEFAULT);

        Log.d("Audio", "converting mp3 to wav " + mp3bytes.length + " bytes");
        InputStream is = new ByteArrayInputStream(mp3bytes);
        convertMp3Stream(is, WAV_FILENAME);

        byte[] re = new byte[0];

        try {
            re = loadFileExternal(WAV_FILENAME);
            Log.d("Audio", "output wav " + re.length + " bytes");

        } catch (Exception e) {
            e.printStackTrace();
        }

        return re;

    }

    private DecodedAudio parseWav(byte[] wavBytes) {
        if (wavBytes.length < 44) {
            throw new IllegalArgumentException("WAV data too short");
        }

        if (!hasAsciiAt(wavBytes, 0, "RIFF") || !hasAsciiAt(wavBytes, 8, "WAVE")) {
            throw new IllegalArgumentException("Unsupported WAV header");
        }

        int offset = 12;
        int channels = 1;
        int sampleRate = 24000;
        int bitsPerSample = 16;
        int dataOffset = -1;
        int dataSize = -1;

        while (offset + 8 <= wavBytes.length) {
            String chunkId = new String(wavBytes, offset, 4);
            int chunkSize = readLittleEndianInt(wavBytes, offset + 4);
            int chunkDataOffset = offset + 8;

            if ("fmt ".equals(chunkId) && chunkDataOffset + 16 <= wavBytes.length) {
                channels = readLittleEndianShort(wavBytes, chunkDataOffset + 2);
                sampleRate = readLittleEndianInt(wavBytes, chunkDataOffset + 4);
                bitsPerSample = readLittleEndianShort(wavBytes, chunkDataOffset + 14);
            } else if ("data".equals(chunkId)) {
                dataOffset = chunkDataOffset;
                dataSize = Math.min(chunkSize, wavBytes.length - chunkDataOffset);
                break;
            }

            offset = chunkDataOffset + chunkSize + (chunkSize % 2);
        }

        if (dataOffset < 0 || dataSize <= 0) {
            throw new IllegalArgumentException("WAV data chunk not found");
        }

        int audioFormat;
        if (bitsPerSample == 8) {
            audioFormat = AudioFormat.ENCODING_PCM_8BIT;
        } else if (bitsPerSample == 16) {
            audioFormat = AudioFormat.ENCODING_PCM_16BIT;
        } else {
            throw new IllegalArgumentException("Unsupported WAV bit depth: " + bitsPerSample);
        }

        byte[] pcmData = Arrays.copyOfRange(wavBytes, dataOffset, dataOffset + dataSize);
        Log.d("Audio", "parsed pcm " + pcmData.length + " bytes, " + sampleRate + " Hz, channels=" + channels + ", bits=" + bitsPerSample);
        return new DecodedAudio(pcmData, sampleRate, channels, audioFormat);
    }

    private boolean hasAsciiAt(byte[] bytes, int offset, String value) {
        if (offset + value.length() > bytes.length) {
            return false;
        }

        for (int i = 0; i < value.length(); i++) {
            if ((byte) value.charAt(i) != bytes[offset + i]) {
                return false;
            }
        }
        return true;
    }

    private int readLittleEndianInt(byte[] bytes, int offset) {
        return (bytes[offset] & 0xff)
                | ((bytes[offset + 1] & 0xff) << 8)
                | ((bytes[offset + 2] & 0xff) << 16)
                | ((bytes[offset + 3] & 0xff) << 24);
    }

    private int readLittleEndianShort(byte[] bytes, int offset) {
        return (bytes[offset] & 0xff) | ((bytes[offset + 1] & 0xff) << 8);
    }

    public byte[] loadFileExternal(String fileName) throws Exception {

        File dir = getAudioFilePath();
        if (!dir.exists()) {
            throw new Exception("TTS Working Dir " + dir.getName() + " not found");
        }

        File file = new File(dir, fileName);

        int size = (int) file.length();
        byte[] bytes = new byte[size];

        try {
            BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
            buf.read(bytes, 0, bytes.length);
            buf.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return bytes;

    }

    //Working storage path
    private File getAudioFilePath() {
        File dir = new File(context.getCacheDir().toString());

        if (!dir.exists()) {
            dir.mkdir();
        }

        return dir;
    }

    public void convertMp3Stream(InputStream is, String fileNameWav) {

        File dir = getAudioFilePath();
        File file_wav = new File(dir, fileNameWav);

        if (file_wav.exists()) {
            file_wav.delete();
        }

        Converter myConverter = new Converter();

        try {

            Converter.ProgressListener progressListener = new Converter.ProgressListener() {
                @Override
                public void converterUpdate(int i, int i1, int i2) {

                }

                @Override
                public void parsedFrame(int i, Header header) {

                }

                @Override
                public void readFrame(int i, Header header) {

                }

                @Override
                public void decodedFrame(int i, Header header, Obuffer obuffer) {

                }

                @Override
                public boolean converterException(Throwable throwable) {
                    return false;
                }
            };

            Decoder.Params params = new Decoder.Params();

            myConverter.convert(is, file_wav.getAbsolutePath(), progressListener, params);

        } catch (JavaLayerException e) {
            e.printStackTrace();
        }

    }


}
