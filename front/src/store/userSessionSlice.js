import { createSlice } from '@reduxjs/toolkit';
import { fetchUserSession } from './thunks/userSessionThunk';

const initialState = {
  name: "",
  username: "",
  loading: true,
  error: null
};

export const userSessionSlice = createSlice({
  name: 'userSession',
  initialState,
  reducers: {},
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
      })
      .addCase(fetchUserSession.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      });
  }
});

export default userSessionSlice.reducer;