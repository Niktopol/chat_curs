"use client"

import Image from "next/image";
import styles from "./modules/profile.module.css"
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faFileImport, faPen } from "@fortawesome/free-solid-svg-icons";
import { useEffect, useState } from "react";
import { useForm } from "react-hook-form";
import { useSelector } from "react-redux";

export default function Profile({ hidden }){
    const session = useSelector((state) => state.userSession);
    const [isEditing, setIsEditing] = useState(false);
    const [userImage, setUserImage] = useState(session.image);
    const [isImageTooBig, setIsImageTooBig] = useState(false);

    const resetEditing = () => {
        setIsEditing(false); 
        setUserImage(session.image);
        setIsImageTooBig(false);
        reset({name: session.name});
    }

    useEffect(() => {
        return () => {
            if (userImage) URL.revokeObjectURL(userImage);
        }
    }, [userImage]);

    useEffect(() => {
        resetEditing();
    }, [session, hidden]);

    const {
        register,
        handleSubmit,
        setValue,
        reset,
        formState: { errors, isSubmitting },
    } = useForm({defaultValues: {name: session.name}});

    const onSubmit = (data) => {
        setIsEditing(false);
        console.log(data);
        //setSubmittedData({nickname: data.nickname})
    };

    return (
        <div className={`${styles.main} ${hidden != null ? (hidden ? styles.slide_left : styles.slide_right) : ""}`}>
            <div className={styles.scroll_wrapper}>
                <p className={styles.id}><span>@</span>{session.username}</p>
                <form className={styles.edit_form} onSubmit={handleSubmit(onSubmit)}>
                    <label htmlFor="profileimage" className={`${styles.image} ${!isEditing ? styles.disabled: ""}`}>
                        <Image src={userImage} alt="" fill draggable={false} style={{objectFit: "cover", zIndex: 2}}></Image>
                        <div className={styles.image_edit_cover}>
                            <FontAwesomeIcon icon={faFileImport} className={styles.image_edit_cover_icon} />
                        </div>
                    </label>
                    <input type="file"
                    accept="image/jpeg"
                    id="profileimage"
                    style={{display: "none"}}
                    {...register("file", {
                        onChange: (e) => {
                            if (e.target.files[0].size < 5 * 1024 * 1024){
                                setUserImage(URL.createObjectURL(e.target.files[0]));
                                setIsImageTooBig(false);
                            } else {
                                setValue("file", null);
                                setIsImageTooBig(true);
                            }
                        },
                        validate: {
                            lessThan5MB: (fileList) => {
                                console.log(fileList);  
                                if (fileList){
                                    if (fileList[0]?.size > 5 * 1024 * 1024) {
                                        setIsImageTooBig(true);
                                        return "Размер изображения больше 5 МБ";
                                    } else {
                                        setIsImageTooBig(false);
                                        return true;
                                    }
                                }
                            }
                        },
                    })}
                    disabled={!isEditing || isSubmitting}/>
                    <p className={styles.file_error_msg}>{isImageTooBig ? "Размер изображения больше 5 МБ" : ""}</p>
                    <div className={styles.nickname}><input
                        placeholder="Введите никнейм"
                        className={styles.nickname_input}
                        {...register("name", {
                            required: "Введите никнейм",
                            maxLength: {
                                value: 20,
                                message: "Значение должно быть короче 20 символов"
                            },
                            pattern: {
                                value: /^\S+$/,
                                message: "Никнейм не должен содержать пробелов"
                            },
                        })}
                        disabled={!isEditing || isSubmitting}/>
                        <p className={styles.error_msg}>{errors.name?.message ? errors.name?.message : ""}</p>
                    </div>
                    {!isEditing && (<input type="button" onClick={() => setIsEditing(true)} value={"Изменить"} className={styles.edit_button}/>)}
                    {isEditing && (<>
                                      <input type="submit" value={"Сохранить"} disabled={isSubmitting} className={styles.edit_button}/>
                                      <input type="button" onClick={resetEditing} value={"Отменить"} disabled={isSubmitting} className={styles.edit_button}/>
                                   </>)}
                </form>
            </div>
        </div>
    );
}