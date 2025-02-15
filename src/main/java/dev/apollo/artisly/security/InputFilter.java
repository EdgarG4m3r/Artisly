package dev.apollo.artisly.security;

import dev.apollo.artisly.models.IndonesianCity;
import dev.apollo.artisly.response.ErrorContainer;
import io.javalin.http.Context;
import io.javalin.http.UploadedFile;
import org.apache.tika.Tika;
import org.apache.tika.mime.MediaType;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InputFilter {

    public static ErrorContainer validateBoolean(String parm, ParamField field, Context context) {
        String input = getParameterValue(parm, field, context);
        ErrorContainer errorContainer = validateEmptyOrNull(parm, field, context);
        if (errorContainer != null) {
            return errorContainer;
        }

        if (!input.equalsIgnoreCase("true") && !input.equalsIgnoreCase("false")) {
            return buildAndAddError(parm, parm + "boolean tidak valid, harus true atau false", context);
        }

        return null;
    }


    public static ErrorContainer validateInt(String param, ParamField field, Context context, int minimum, int maximum) {
        String input = getParameterValue(param, field, context);
        ErrorContainer errorContainer = validateEmptyOrNull(param, field, context);
        if (errorContainer != null) {
            return errorContainer;
        }

        try {
            int value = Integer.parseInt(input);

            if (value < minimum) {
                return buildAndAddError(param, "nilai terlalu kecil, harus lebih dari " + minimum, context);
            }

            if (value > maximum) {
                return buildAndAddError(param, "nilai terlalu besar, harus kurang dari " + maximum, context);
            }

            return null;
        } catch (NumberFormatException e) {
            return buildAndAddError(param, "nilai tidak valid, harus berupa integer/angka", context);
        }
    }

    public static ErrorContainer validateLocalDate(String param, ParamField field, Context context) {
        String input = getParameterValue(param, field, context);
        ErrorContainer errorContainer = validateEmptyOrNull(param, field, context);
        if (errorContainer != null) {
            return errorContainer;
        }

        try {
            LocalDate.parse(input);
            return null;
        } catch (DateTimeParseException e) {
            return buildAndAddError(param, "nilai tidak valid, harus berupa tanggal", context);
        }
    }

    public static ErrorContainer validateDouble(String param, ParamField field, Context context, double minimum, double maximum) {
        String input = getParameterValue(param, field, context);
        ErrorContainer errorContainer = validateEmptyOrNull(param, field, context);
        if (errorContainer != null) {
            return errorContainer;
        }

        try {
            double value = Double.parseDouble(input);

            if (value < minimum) {
                return buildAndAddError(param, "nilai terlalu kecil, harus lebih dari " + minimum, context);
            }

            if (value > maximum) {
                return buildAndAddError(param, "nilai terlalu besar, harus kurang dari " + maximum, context);
            }

            return null;
        } catch (NumberFormatException e) {
            return buildAndAddError(param, "nilai tidak valid, harus berupa double/angka", context);
        }
    }

    public static ErrorContainer validateKTP(String param, ParamField field, Context context) {
        // credit: https://www.huzefril.com/posts/regex/regex-ktp/
        String regex = "^(1[1-9]|21|[37][1-6]|5[1-3]|6[1-5]|[89][12])\\d{2}\\d{2}([04][1-9]|[1256][0-9]|[37][01])(0[1-9]|1[0-2])\\d{2}\\d{4}$";
        Pattern pattern = Pattern.compile(regex);

        String input = getParameterValue(param, field, context);
        ErrorContainer errorContainer = validateEmptyOrNull(param, field, context);
        if (errorContainer != null) {
            return errorContainer;
        }

        Matcher matcher = pattern.matcher(input);
        if (!matcher.matches()) {
            return buildAndAddError(param, "KTP tidak valid. Harus berupa 16 digit angka, sesuai dengan format KTP", context);
        }

        return null;
    }


    public static ErrorContainer validateString(String param, ParamField field, Context context, String[] expected) {
        String value = getParameterValue(param, field, context);
        ErrorContainer errorContainer = validateEmptyOrNull(param, field, context);
        if (errorContainer != null) {
            return errorContainer;
        }

        if (!Arrays.asList(expected).contains(value)) {
            return buildAndAddError(param, "Pilihan tidak valid, harus salah satu dari " + Arrays.toString(expected), context);
        }
        return null;
    }

    public static ErrorContainer validateString(String param, ParamField field, Context context, int length) {
        String value = getParameterValue(param, field, context);

        ErrorContainer errorContainer = validateEmptyOrNull(param, field, context);
        if (errorContainer != null) {
            return errorContainer;
        }

        if (value.length() > length) {
            return buildAndAddError(param, "String terlalu panjang, maksimal " + length + " karakter", context);
        }

        String newValue = Jsoup.clean(value, Safelist.none());

        if (!value.equals(newValue)) {
            return buildAndAddError(param, "String tidak valid, tidak boleh mengandung tag HTML", context);
        }
        return null;
    }


    public static ErrorContainer validateQuantity(String param, ParamField field, Context context) {
        int maxQuantity = 10;
        int minQuantity = 1;
        String value = getParameterValue(param, field, context);

        ErrorContainer errorContainer = validateEmptyOrNull(param, field, context);
        if (errorContainer != null) {
            return errorContainer;
        }

        if (!value.matches("[0-9]+")) {
            return buildAndAddError(param, "Kuantitas tidak valid, harus berupa angka", context);
        }

        int intValue = Integer.parseInt(value);
        if (intValue > maxQuantity || intValue < minQuantity) {
            return buildAndAddError(param, "Kuantitas tidak valid, harus diantara " + minQuantity + " dan " + maxQuantity, context);
        }

        return null;
    }

    public static ErrorContainer validateUUID(String param, ParamField field, Context context) {
        UUID uuid;
        String value = getParameterValue(param, field, context);

        ErrorContainer errorContainer = validateEmptyOrNull(param, field, context);
        if (errorContainer != null) {
            return errorContainer;
        }

        try {
            uuid = UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            return buildAndAddError(param, "UUID tidak valid, harus berupa UUID yang valid", context);
        }

        return null;
    }

    public static ErrorContainer validateEmail(String field, ParamField paramField, Context context) {
        String value = getParameterValue(field, paramField, context);

        ErrorContainer errorContainer = validateEmptyOrNull(field, paramField, context);
        if (errorContainer != null) {
            return errorContainer;
        }

        if (value.length() > 255) {
            return buildAndAddError(field, "Email terlalu panjang, maksimal 255 karakter", context);
        }

        if (!value.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            return buildAndAddError(field, "Format email tidak valid", context);
        }

        return null;
    }

    public static ErrorContainer validateName(String field, ParamField paramField, Context context) {
        String value = getParameterValue(field, paramField, context);

        if (value == null) {
            return buildAndAddError(field, "Nama tidak boleh kosong", context);
        }

        if (value.isEmpty()) {
            return buildAndAddError(field, "Nama tidak boleh kosong", context);
        }

        if (value.length() > 255) {
            return buildAndAddError(field, "Nama terlalu panjang, maksimal 255 karakter", context);
        }

        return null;
    }

    public static ErrorContainer validatePassword(String field, ParamField paramField, Context context) {
        String value = getParameterValue(field, paramField, context);

        if (value == null) {
            return buildAndAddError(field, "Password tidak boleh kosong", context);
        }

        if (value.isEmpty()) {
            return buildAndAddError(field, "Password tidak boleh kosong", context);
        }

        if (value.length() > 255) {
            return buildAndAddError(field, "Password terlalu panjang, maksimal 255 karakter", context);
        }

        return null;
    }

    public static ErrorContainer validatePhoneNumber(String field, ParamField paramField, Context context)
    {

        String value = getParameterValue(field, paramField, context);
        ErrorContainer errorContainer = validateEmptyOrNull(field, paramField, context);
        String regex = "^(\\+62|0)[0-9]{8,15}$";
        Pattern pattern = Pattern.compile(regex);

        if (errorContainer != null)
        {
            return errorContainer;
        }

        if (value.length() > 20)
        {
            return buildAndAddError(field, "Nomor telepon terlalu panjang, maksimal 20 karakter", context);
        }

        Matcher matcher = pattern.matcher(value);
        if (!matcher.matches())
        {
            return buildAndAddError(field, "Nomor telepon tidak valid. Harus sesuai format contoh: +6281234567890", context);
        }

        return null;
    }

    public static ErrorContainer validateToken(String field, ParamField fieldLocation, Context context)
    {
        String tokenRegex = "^(?:[A-Za-z0-9+/]{4})*(?:[A-Za-z0-9+/]{2}==|[A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{4})$";

        String value = getParameterValue(field, fieldLocation, context);
        ErrorContainer errorContainer = validateEmptyOrNull(field, fieldLocation, context);
        if (errorContainer != null)
        {
            return errorContainer;
        }

        if (value.length() > 255)
        {
            return buildAndAddError(field, "Token terlalu panjang, maksimal 255 karakter", context);
        }

        Pattern pattern = Pattern.compile(tokenRegex);
        Matcher matcher = pattern.matcher(value);
        if (!matcher.matches())
        {
            return buildAndAddError(field, "Token tidak valid. Refresh halaman dan coba login kembali", context);
        }

        return null;
    }

    public static ErrorContainer validateVerificationCode(String field, ParamField fieldLocation, Context context)
    {
        String verificationCodeRegex = "^\\d{8}$";
        ErrorContainer errorContainer = validateEmptyOrNull(field, fieldLocation, context);
        if (errorContainer != null)
        {
            return errorContainer;
        }

        String value = getParameterValue(field, fieldLocation, context);
        if (value.length() > 8)
        {
            return buildAndAddError(field, "Kode verifikasi harus 8 digit", context);
        }

        Pattern pattern = Pattern.compile(verificationCodeRegex);
        Matcher matcher = pattern.matcher(value);
        if (!matcher.matches())
        {
            return buildAndAddError(field, "Kode verifikasi tidak valid", context);
        }

        return null;
    }

    public static ErrorContainer validateCityName(String field, ParamField paramField, Context context)
    {
        String value = getParameterValue(field, paramField, context);
        ErrorContainer errorContainer = validateEmptyOrNull(field, paramField, context);
        if (errorContainer != null)
        {
            return errorContainer;
        }

        if (value.length() > 50)
        {
            return buildAndAddError(field, "Nama kota maksimal 50 karakter", context);
        }

        try
        {
            IndonesianCity.valueOf(value.toUpperCase().replaceAll(" ", "_"));
        }
        catch (IllegalArgumentException e)
        {
            return buildAndAddError(field, "Nama kota tidak tersedia", context);
        }

        return null;
    }

    public static ErrorContainer validateAddressNotes(String field, ParamField paramField, Context context)
    {

        String value = getParameterValue(field, paramField, context);
        ErrorContainer errorContainer = validateEmptyOrNull(field, paramField, context);
        if (errorContainer != null)
        {
            return errorContainer;
        }

        if (value.length() > 256)
        {
            return buildAndAddError(field, "Notes maximal 256 huruf", context);
        }

        String newValue = Jsoup.clean(value, Safelist.none());


        if (!newValue.equals(value))
        {
            return buildAndAddError(field, "Notes mengandung simbol yang tidak diperbolehkan", context);
        }

        return null;
    }

    public static ErrorContainer validateAddress(String field, ParamField paramField, Context context)
    {

        String value = getParameterValue(field, paramField, context);
        ErrorContainer errorContainer = validateEmptyOrNull(field, paramField, context);
        if (errorContainer != null)
        {
            return errorContainer;
        }

        if (value.length() > 400)
        {
            return buildAndAddError(field, "Alamat maximal 400 huruf", context);
        }

        String newValue = Jsoup.clean(value, Safelist.none());

        if (!newValue.equals(value))
        {
            return buildAndAddError(field, "Alamat mengandung simbol yang tidak diperbolehkan", context);
        }

        return null;
    }


    public static ErrorContainer validateUploadedFiles(String field, int maxEachSizeInKB, MediaType[] allowedFilesExtension, int maxTotalSizeInKB, int maxFilesCount, Context context)
    {
        if (context.uploadedFiles(field) == null)
        {
            return buildAndAddError(field, field + " is empty, received null", context);
        }

        if (context.uploadedFiles(field).isEmpty())
        {
            return buildAndAddError(field, field + " is empty", context);
        }

        if (context.uploadedFiles(field).size() > maxFilesCount)
        {
            return buildAndAddError(field, field + " is invalid, must be less than " + maxFilesCount + " files", context);
        }

        AtomicLong totalSize = new AtomicLong(0);

        for (int i = 0; i < context.uploadedFiles(field).size(); i++)
        {
            UploadedFile uploadedFile = context.uploadedFiles(field).get(i);
            String identifier = field + "[" + i + "]";
            ErrorContainer errorContainer = validateFileName(identifier, uploadedFile.filename(), context);
            if (errorContainer != null)
            {
                return errorContainer;
            }


            try(InputStream inputStream = uploadedFile.content())
            {
                if (inputStream == null)
                {
                    return buildAndAddError(identifier, "input stream in array index " + identifier + " is empty", context);
                }
                if (inputStream.available() == 0)
                {
                    return buildAndAddError(identifier, "input stream in array index " + identifier + " is empty", context);
                }
                if (inputStream.available() > maxEachSizeInKB * 1024)
                {
                    return buildAndAddError(identifier, "input stream in array index " + identifier + " is invalid, must be less than " + maxEachSizeInKB + " KB", context);
                }

                totalSize.addAndGet(inputStream.available());

                if (totalSize.get() > maxTotalSizeInKB * 1024)
                {
                    return buildAndAddError(identifier, "total size of uploaded images " + identifier + " is invalid, must be less than " + maxTotalSizeInKB + " KB", context);
                }

                errorContainer = validateFileExtension(identifier, inputStream, allowedFilesExtension, context);
                if (errorContainer != null)
                {
                    return errorContainer;
                }

            }
            catch (IOException e)
            {
                return buildAndAddError(identifier, "input stream in array index " + identifier + " is invalid, must be a valid input stream", context);
            }
        }
        return null;
    }

    private static ErrorContainer validateFileExtension(String identifier, InputStream inputStream, MediaType[] allowedFilesExtension, Context context) throws IOException
    {
        boolean isAllowed = false;
        MediaType currentMediaType = MediaType.parse(new Tika().detect(inputStream));
        for (MediaType mediaType : allowedFilesExtension)
        {
            if (currentMediaType.equals(mediaType))
            {
                isAllowed = true;
                break;
            }
        }
        if (!isAllowed)
        {
            return buildAndAddError(identifier, "file extension in array index " + identifier + " is invalid, must be one of " + Arrays.toString(allowedFilesExtension), context);
        }
        return null;
    }

    private static ErrorContainer validateFileName(String identifier, String fileName, Context context)
    {
        if (fileName == null)
        {
            return buildAndAddError(identifier, "filename in array index " + identifier + " is empty, received null", context);
        }
        if (fileName.isEmpty())
        {
            return buildAndAddError(identifier, "filename in array index " + identifier + " is empty", context);
        }
        if (fileName.length() > 255)
        {
            return buildAndAddError(identifier, "filename in array index " + identifier + " is too long, must be less than " + 255 + " characters", context);
        }

        if (fileName.contains(".."))
        {
            return buildAndAddError(identifier, "filename in array index " + identifier + " is invalid, must be a valid filename", context);
        }

        if (fileName.contains("/"))
        {
            return buildAndAddError(identifier, "filename in array index " + identifier + " is invalid, must be a valid filename", context);
        }

        if (fileName.contains("\\"))
        {
            return buildAndAddError(identifier, "filename in array index " + identifier + " is invalid, must be a valid filename", context);
        }

        if (fileName.contains(":"))
        {
            return buildAndAddError(identifier, "filename in array index " + identifier + " is invalid, must be a valid filename", context);
        }

        if (fileName.contains("*"))
        {
            return buildAndAddError(identifier, "filename in array index " + identifier + " is invalid, must be a valid filename", context);
        }

        if (fileName.contains("?"))
        {
            return buildAndAddError(identifier, "filename in array index " + identifier + " is invalid, must be a valid filename", context);
        }

        if (fileName.contains("\""))
        {
            return buildAndAddError(identifier, "filename in array index " + identifier + " is invalid, must be a valid filename", context);
        }
        return null;

    }

    private static ErrorContainer validateEmptyOrNull(String param, ParamField field, Context context) {
        String value = getParameterValue(param, field, context);
        String message = param.substring(0, 1).toUpperCase() + param.substring(1) + " tidak boleh kosong";
        if (value == null) {
            return buildAndAddError(param, message, context);
        }
        if (value.isEmpty()) {
            return buildAndAddError(param, message, context);
        }
        return null;
    }

    private static String getParameterValue(String param, ParamField field, Context context) {
        String value = null;
        if (field.equals(ParamField.FORM)) {
            value = context.formParam(param);
        } else if (field.equals(ParamField.QUERY)) {
            value = context.queryParam(param);
        } else if (field.equals(ParamField.PATH)) {
            value = context.pathParam(param);
        } else if (field.equals(ParamField.HEADER)) {
            value = context.header(param);
        }
        return value;
    }

    private static ErrorContainer buildAndAddError(String field, String message, Context context) {
        ErrorContainer errorContainer = new ErrorContainer(field, message);
        addErrorContainerToContext(context, errorContainer);
        return errorContainer;
    }


    private static void addErrorContainerToContext(Context context, ErrorContainer errorContainer)
    {
        if (context.attribute("hasErrors") == null)
        {
            context.attribute("errors", null);
        }
        if (context.attribute("errors") == null)
        {
            context.attribute("errors", new ArrayList<ErrorContainer>());
            context.attribute("hasErrors", true);
        }
        ArrayList<ErrorContainer> errors = context.attribute("errors");
        errors.add(errorContainer);
    }

}
