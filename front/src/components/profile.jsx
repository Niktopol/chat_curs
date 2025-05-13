"use client"

import Image from "next/image";
import styles from "./modules/profile.module.css"
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faFileImport, faPen, faRightFromBracket } from "@fortawesome/free-solid-svg-icons";
import { useEffect, useState } from "react";
import { useForm } from "react-hook-form";
import { useDispatch, useSelector } from "react-redux";
import { useRouter } from "next/navigation";
import { setData } from "@/store/userSessionSlice";

export default function Profile({ hidden }){
    const session = useSelector((state) => state.userSession);
    const dispatch = useDispatch();
    const [isEditing, setIsEditing] = useState(false);
    const [doDelProfilePic, setDoDelProfilePic] = useState(false);
    const [userImage, setUserImage] = useState(session.image);
    const [imageErrorMsg, setImageErrorMsg] = useState("");
    const router = useRouter();

    const resetEditing = () => {
        setIsEditing(false); 
        setUserImage(session.image);
        setImageErrorMsg("");
        setDoDelProfilePic(false);
        reset({name: session.name});
    }

    useEffect(() => {
        if (doDelProfilePic) setUserImage("/default_user.svg");
    }, [doDelProfilePic]);

    useEffect(() => {
        return () => {
            if (userImage.startsWith('blob:')) {
                if (userImage) URL.revokeObjectURL(userImage);
            }
        }
    }, [userImage]);

    useEffect(() => {
        resetEditing();
    }, [hidden, session]);

    const {
        register,
        handleSubmit,
        setValue,
        reset,
        formState: { errors, isSubmitting },
    } = useForm({defaultValues: {name: session.name}});

    const logout = async () => {
        try {
            await fetch("http://localhost:8080/logout", {
                method: "POST",
                credentials: "include"
            });
            router.push("/login");
        } catch (e) {
            router.push("/login");
        }
    }

    const onSubmit = async (data) => {
        try {
            if (data.file) {
                const formData = new FormData();
                formData.append('file', data.file[0]);
                const res = await fetch("http://localhost:8080/user/profilepic", {
                    method: "PATCH",
                    credentials: "include",
                    body: formData
                });

                if (!res.ok) {
                    setUserImage(session.image);
                    setValue("file", null);
                    setImageErrorMsg((await res.json())?.error);
                    return;
                }

            } else if (doDelProfilePic) {
                const res = await fetch("http://localhost:8080/user/profilepic", {
                    method: "DELETE",
                    credentials: "include"
                });

                if (!res.ok) {
                    throw new Error((await res.json())?.error);
                }
            }

            const res = await fetch("http://localhost:8080/user/profile", {
                method: "PATCH",
                credentials: "include",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ name: data.name }),
            });

            if (!res.ok) {
                throw new Error((await res.json())?.error);
            }

            dispatch(setData({name: data.name, image: userImage}));
            setIsEditing(false);
        } catch (e) {
            router.push("/login");
        }
    };

    return (
        <div className={`${styles.main} ${hidden != null ? (hidden ? styles.slide_left : styles.slide_right) : ""}`}>
            <div onClick={logout} className={styles.logout}>
                <FontAwesomeIcon icon={faRightFromBracket} className={styles.logout_image}/>
            </div>
            <div className={styles.hide_wrapper}>
                <div className={styles.scroll_wrapper}>
                    <p className={styles.id}><span>@</span>{session.username}</p>
                    <form className={styles.edit_form} onSubmit={handleSubmit(onSubmit)}>
                        <div className={styles.image_wrapper}>
                            <label htmlFor="profileimage" className={`${styles.image} ${!isEditing ? styles.disabled: ""}`}>
                                <Image src={userImage} alt="" fill draggable={false} style={{objectFit: "cover", zIndex: 2}}></Image>
                                <div className={styles.image_edit_cover}>
                                    <FontAwesomeIcon icon={faFileImport} className={styles.image_edit_cover_icon} />
                                </div>
                            </label>
                        </div>
                        <input type="file"
                        accept="image/jpeg"
                        id="profileimage"
                        style={{display: "none"}}
                        {...register("file", {
                            onChange: (e) => {
                                if (e.target.files[0].size < 5 * 1024 * 1024) {
                                    setUserImage(URL.createObjectURL(e.target.files[0]));
                                    setDoDelProfilePic(false);
                                    setImageErrorMsg("")
                                } else {
                                    setUserImage(session.image);
                                    setValue("file", null);
                                    setImageErrorMsg("Размер изображения больше 5 МБ")
                                }
                            },
                            validate: {
                                lessThan5MB: (fileList) => {
                                    if (fileList){
                                        if (fileList[0]?.size > 5 * 1024 * 1024) {
                                            setImageErrorMsg("Размер изображения больше 5 МБ")
                                            return "Размер изображения больше 5 МБ";
                                        } else {
                                            setImageErrorMsg("")
                                            return true;
                                        }
                                    }
                                }
                            },
                        })}
                        disabled={!isEditing || isSubmitting}/>
                        <p className={styles.file_error_msg}>{imageErrorMsg}</p>
                        {isEditing && (<input type="button" onClick={() => setDoDelProfilePic(true)} value={"Удалить фото"} className={styles.edit_button}/>)}
                        <div className={styles.nickname}><input
                            placeholder={session.name}
                            autoComplete="off"
                            autoCorrect="off"
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
            
        </div>
    );
}