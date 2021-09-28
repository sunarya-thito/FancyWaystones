package thito.fancywaystones.proxy.message;

import java.io.*;

public abstract class Message implements Serializable {
    public byte[] write() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream)) {
            objectOutputStream.writeObject(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return outputStream.toByteArray();
    }
    public static <T extends Message> T read(byte[] b) {
        try (ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(b))) {
            return (T) objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
           e.printStackTrace();
        }
        return null;
    }
}
