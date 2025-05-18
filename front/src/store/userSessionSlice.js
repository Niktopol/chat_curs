import { createSlice } from '@reduxjs/toolkit';
import { fetchUserSession } from './thunks/userSessionThunk';

const initialState = {
  name: "",
  username: "",
  loading: true,
  error: null,
  image: "/chat_curs/default_user.svg"
};

export const userSessionSlice = createSlice({
  name: 'userSession',
  initialState,
  reducers: {
    setData: (state, action) => {
      if (action.payload?.name) {
        state.name = action.payload.name;
      }
      if (action.payload?.image) {
        state.image = action.payload.image;
      }
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(fetchUserSession.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchUserSession.fulfilled, (state, action) => {
        state.loading = false;
        state.error = null;
        state.name = action.payload.name;
        state.username = action.payload.username;
        if (action.payload?.image) {
          state.image = action.payload.image;
        } else {
          state.image = "/chat_curs/default_user.svg";
        }
        
      })
      .addCase(fetchUserSession.rejected, (state, action) => {
        state.name = "";
        state.username = "";
        state.loading = false;
        state.error = action.payload;
        state.image = "/chat_curs/default_user.svg";
      });
  }
});

export const { setData } = userSessionSlice.actions;
export default userSessionSlice.reducer;